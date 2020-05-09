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
package com.viiyue.plugins.validator.spring.beans;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.viiyue.plugins.validator.Validator;
import com.viiyue.plugins.validator.common.Constants;
import com.viiyue.plugins.validator.metadata.result.ElementResult;
import com.viiyue.plugins.validator.metadata.result.FragmentResult;
import com.viiyue.plugins.validator.metadata.result.ValidatedResult;
import com.viiyue.plugins.validator.spring.ValidatorLite;
import com.viiyue.plugins.validator.spring.bindings.ParameterBindingResult;
import com.viiyue.plugins.validator.spring.exception.ValidatedException;
import com.viiyue.plugins.validator.spring.utils.LocaleUtils;

/**
 * Rewrite handler method parameter resolver and add data validation after
 * parameter processing is complete.
 *
 * @author tangxbai
 * @since 1.0.0
 */
public final class ValidationHandlerMethodArgumentResolverComposite extends HandlerMethodArgumentResolverComposite {

	private static final String FORWARD_ATTRIBUTE_PREFIX = "javax.servlet.forward";
	private static final String BINDING_RESULT = ValidationHandlerMethodArgumentResolverComposite.class.getName() + ".BINDING_RESULT";
	private static final String VALIDATED_RESULT = ValidationHandlerMethodArgumentResolverComposite.class.getName() + ".VALIDATED_RESULT";
	
	public ValidationHandlerMethodArgumentResolverComposite( HandlerMethodArgumentResolverComposite resolvers ) {
		super.addResolvers( resolvers.getResolvers() );
	}
	
	@Override
	public Object resolveArgument( MethodParameter mp, @Nullable ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory ) throws Exception {
		
		BindingResult bindingResult = null;
		final Class<?> parameterType = mp.getParameterType();
		final boolean isParameterErrors = Errors.class.isAssignableFrom( parameterType );
		Object bindingResultObject = webRequest.getAttribute( BINDING_RESULT, RequestAttributes.SCOPE_REQUEST );
		
		// The 'errors' parameter will be handled by 'ErrorsMethodArgumentResolver'
		// @org.springframework.web.method.annotation.ErrorsMethodArgumentResolver
		if ( isParameterErrors ) {
			if ( bindingResultObject != null ) {
				bindingResult = ( BindingResult ) bindingResultObject;
				Map<String, Object> bindingResultModel = bindingResult.getModel();
				mavContainer.removeAttributes( bindingResultModel );
				mavContainer.addAllAttributes( bindingResultModel );
				clearAttributes( webRequest, mp, bindingResult, isParameterErrors );
			}
			return super.resolveArgument( mp, mavContainer, webRequest, binderFactory );
		}
		
		// Get the parsed value by the framework
		final Object argument = super.resolveArgument( mp, mavContainer, webRequest, binderFactory );
		boolean isValidated = ValidatorLite.getValidatedResult( webRequest ) != null; 
		
		// 1) Prevent duplicate validation
		// 2) Program internal forwarding does not need to handle parameter validation
		if ( isValidated || isInternalForwarding( webRequest ) ) {
			return argument;
		}
		
		// Validating non-bean ordinary parameters
		Method method = mp.getMethod();
		String methodName = method.getName();
		Parameter parameter = mp.getParameter();
		Validated validated = getValidatedAnnotation( mp, parameter );
		if ( validated == null ) {
			clearAttributes( webRequest, mp, bindingResult, isParameterErrors );
			return argument;
		}
		
		if ( bindingResultObject != null ) {
			bindingResult = ( BindingResult ) bindingResultObject;
		}
		
		// Initialize the data binder, because there is no specific object for ordinary parameters, 
		// so use Map as the data source here.
		else {
			Map<String, Object> target = new HashMap<String, Object>( mp.getExecutable().getParameterCount() );
			WebDataBinder binder = binderFactory.createBinder( webRequest, target, methodName );
			binder.initDirectFieldAccess();
			bindingResult = binder.getBindingResult();
			webRequest.setAttribute( BINDING_RESULT, bindingResult, RequestAttributes.SCOPE_REQUEST ); // Temporary cache
		}
		
		// The validator supports internationalized message display, 
		// but needs to get the current locale from the spring framework.
		Locale locale = LocaleUtils.switchLocale();

		// Validation of common parameters
		String parameterName = mp.getParameterName();
		String containingName = mp.getContainingClass().getName();
		String defaultMessage = "{" + containingName + "." + method.getName() + "." + parameterName + "}";
		ValidatedResult result = Validator.validateParameter( argument, parameter, parameterName, defaultMessage, locale, validated.value() );
		
		// Integration of multiple validation parameters
		ValidatedResult validatedResult = null;
		Object validatedResultObject = webRequest.getAttribute( VALIDATED_RESULT, RequestAttributes.SCOPE_REQUEST );
		if ( validatedResultObject == null ) {
			validatedResult = result;
			webRequest.setAttribute( VALIDATED_RESULT, validatedResult, RequestAttributes.SCOPE_REQUEST );
		} else {
			validatedResult = ( ValidatedResult ) validatedResultObject;
			validatedResult.merge( result );
		}
		
		// Add parameter data to the validation binding result
		if ( bindingResult instanceof ParameterBindingResult ) {
			ParameterBindingResult pbr = ( ( ParameterBindingResult ) bindingResult );
			pbr.setValidated( validatedResult );
			pbr.putParameter( parameterName, argument, parameterType );
		}
		
		// Append each validation result to BindingResult
		if ( !result.isPassed() ) {
			ElementResult rejectedResult = result.getLastRejectedResult();
			for ( FragmentResult fr : ( List<FragmentResult> ) rejectedResult.getResult() ) { // Updated in v1.0.3
				String basicMessageCode = Constants.DEFAULT_MESSAGE_KEY_PREFIX + "." + fr.getFragment();
				String [] errorCodes = bindingResult.resolveMessageCodes( basicMessageCode, parameterName );
				bindingResult.addError( new FieldError( methodName, parameterName, argument, false, errorCodes, fr.getArguments(), fr.getErrorMessage() ) );
			}
		}
		
		// Automatically clear cache data when the last parameter is processed
		clearAttributes( webRequest, mp, bindingResult, isParameterErrors );
		return argument;
	}
	
	/**
	 * Get validation mark annotation
	 * 
	 * @param mp the spring method parameter object
	 * @param parameter method parameter object
	 * @return annotation mark object, {@code null} if not found.
	 */
	private Validated getValidatedAnnotation( MethodParameter mp, Parameter parameter ) {
		// Method parameter annotation
		Validated validated = parameter.getAnnotation( Validated.class );
		// Method annotation
		if ( validated == null ) {
			validated = mp.getExecutable().getAnnotation( Validated.class );
		}
		return validated;
	}
	
	/**
	 * Remove cached data when the last argument is processed
	 * 
	 * @param webRequest the current request object
	 * @param mp the spring method parameter object
	 * @param result the validate binding results
	 * @param isParameterErrors whether it is a wrong parameter?
	 * @throws BindException If there are any binding errors, and the last parameter is of type {@link Errors}.
	 */
	private void clearAttributes( NativeWebRequest webRequest, MethodParameter mp, BindingResult result, boolean isParameterErrors ) throws BindException {
		int index = mp.getParameterIndex();
		int totalNumber = mp.getExecutable().getParameterCount();
		if ( index == totalNumber - 1 ) { // Last argument
			ValidatorLite.removeValidatedResult( webRequest );
			webRequest.removeAttribute( BINDING_RESULT, RequestAttributes.SCOPE_REQUEST );
			webRequest.removeAttribute( VALIDATED_RESULT, RequestAttributes.SCOPE_REQUEST );
			if ( !isParameterErrors && result != null && result.hasErrors() ) {
				throw new ValidatedException( result );
			}
		}
	}
	
	/**
	 * Determine if it is an internally forwarded request
	 * 
	 * @param request the current request
	 * @return {@code true} for internal request forwarding, {@code false} is not.
	 */
	private boolean isInternalForwarding( NativeWebRequest request ) {
		String [] attributeNames = request.getAttributeNames( RequestAttributes.SCOPE_REQUEST );
		for ( String attributeName : attributeNames ) {
			if ( attributeName.startsWith( FORWARD_ATTRIBUTE_PREFIX ) ) {
				return true;
			}
		}
		return false;
	}

}
