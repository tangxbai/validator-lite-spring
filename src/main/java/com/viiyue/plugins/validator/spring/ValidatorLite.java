/**
 * Copyright (C) 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.viiyue.plugins.validator.spring;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.SmartValidator;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.viiyue.plugins.validator.Validator;
import com.viiyue.plugins.validator.common.Constants;
import com.viiyue.plugins.validator.metadata.result.ElementResult;
import com.viiyue.plugins.validator.metadata.result.FragmentResult;
import com.viiyue.plugins.validator.metadata.result.ValidatedResult;
import com.viiyue.plugins.validator.spring.bindings.BeanBindingResult;
import com.viiyue.plugins.validator.spring.utils.LocaleUtils;

/**
 * Java bean parameters for validating spring injection
 *
 * @author tangxbai
 * @since 1.0.0
 */
public final class ValidatorLite implements SmartValidator {
	
	private static final String VALIDATED = ValidatorLite.class.getName() + ".VALIDATED";

	@Override
	public boolean supports( Class<?> clazz ) {
		return !BeanUtils.isSimpleValueType( clazz );
	}

	@Override
	public void validate( Object target, Errors errors ) {
		validate( target, errors, Constants.DEFAULT_OBJECT_GROUPS );
	}

	@Override
	public void validate( Object target, Errors errors, Object ... groups ) {
		Locale currentLocale = LocaleUtils.switchLocale();
		ValidatedResult result = Validator.validateBean( target, currentLocale, ( Class<?> [] ) groups );
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		setValidatedResult( attributes, result );
		if ( !result.isPassed() ) {
			processBindingErrors( result, errors );
		}
		if ( errors instanceof BeanBindingResult ) {
			( ( BeanBindingResult ) errors ).setValidated( result );
		}
	}
	
	/**
	 * To handle data binding errors, the original validation result is
	 * {@code validator-lite}, so we need to convert the result into a result object
	 * that the Spring framework can recognize.
	 * 
	 * @param result the data validated results of the plug-in
	 * @param errors the spring validation error binding object
	 */
	private void processBindingErrors( ValidatedResult result, Errors errors  ) {
		for ( ElementResult rejected : result.getRejectedResults() ) {
			String field = rejected.getField();
			FieldError fieldError = errors.getFieldError( field );
			if ( fieldError == null || !fieldError.isBindingFailure() ) {
				if ( rejected.isTypeOf( ValidatedResult.class ) ) {
					errors.pushNestedPath( field );
					processBindingErrors( rejected.getBeanResult(), errors );
					errors.popNestedPath();
				} else {
					processFragmentErrors( rejected, errors, field );
				}
			}
		}
	}
	
	/**
	 * Handle validation target fragmentation error results
	 * 
	 * @param rejected the element validation results
	 * @param errors the spring validation error binding object
	 * @param field the parameter field name
	 */
	private void processFragmentErrors( ElementResult rejected, Errors errors, String field ) {
		for ( FragmentResult result : rejected.getFragmentResults() ) {
			// Can do custom FieldError registration with invalid value from Validator, 
			// as necessary for Validator compatibility (non-indexed set path in field)
			if ( errors instanceof BindingResult ) {
				BindingResult bindingResult = ( BindingResult ) errors;
				String nestedField = errors.getNestedPath() + field;
				if ( StringUtils.isEmpty( nestedField ) ) {
					String basicMessageCode = Constants.DEFAULT_MESSAGE_KEY_PREFIX + "." + result.getFragment();
					String [] errorCodes = bindingResult.resolveMessageCodes( basicMessageCode );
					ObjectError error = new ObjectError( errors.getObjectName(), errorCodes, result.getArguments(), result.getErrorMessage() );
					error.wrap( result );
					bindingResult.addError( error );
				} else {
					String basicMessageCode = Constants.DEFAULT_MESSAGE_KEY_PREFIX + "." + result.getErrorCode();
					String[] errorCodes = bindingResult.resolveMessageCodes( basicMessageCode, field );
					FieldError error = new FieldError( errors.getObjectName(), nestedField, rejected.getFieldValue(),
							false, errorCodes, result.getArguments(), result.getErrorMessage() );
					error.wrap( result );
					bindingResult.addError( error );
				}
			} else {
				// Got no BindingResult - can only do standard rejectValue call
				// with automatic extraction of the current field value
				errors.rejectValue( field, result.getErrorCode(), result.getArguments(), result.getErrorMessage() );
			}
		}
	}
	
	/**
	 * Get staging validation results
	 * 
	 * @param attributes the spring request attributes
	 * @return the result of the validated
	 */
	public static Object getValidatedResult( RequestAttributes attributes ) {
		return attributes.getAttribute( VALIDATED, RequestAttributes.SCOPE_REQUEST );
	}
	
	/**
	 * Remove validation results from the request attributes
	 * 
	 * @param attributes the spring request attributes
	 */
	public static void removeValidatedResult( RequestAttributes attributes ) {
		attributes.removeAttribute( VALIDATED, RequestAttributes.SCOPE_REQUEST );
	}
	
	/**
	 * Temporarily store validation results in request attributes
	 * 
	 * @param attributes the spring request attributes
	 * @param result the validated result
	 */
	public static void setValidatedResult( RequestAttributes attributes, Object result ) {
		attributes.setAttribute( VALIDATED, result, RequestAttributes.SCOPE_REQUEST );
	}

}
