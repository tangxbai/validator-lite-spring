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

import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.validation.AbstractPropertyBindingResult;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;

import com.viiyue.plugins.validator.spring.bindings.BeanBindingResult;
import com.viiyue.plugins.validator.spring.bindings.ParameterBindingResult;

/**
 * Wrapped {@link ExtendedServletRequestDataBinder}, used to wrap the original
 * data validation binding results.
 *
 * @author tangxbai
 * @since 1.0.0
 */
public class ValidationServletRequestDataBinder extends ExtendedServletRequestDataBinder {
	
	@Nullable
	private MessageCodesResolver messageCodesResolver; 

	public ValidationServletRequestDataBinder( @Nullable Object target ) {
		super( target );
	}

	public ValidationServletRequestDataBinder( @Nullable Object target, String objectName ) {
		super( target, objectName );
	}

	@Override
	public void setMessageCodesResolver( @Nullable MessageCodesResolver messageCodesResolver ) {
		super.setMessageCodesResolver( messageCodesResolver );
		this.messageCodesResolver = messageCodesResolver;
	}
	
	@Override
	protected AbstractPropertyBindingResult createDirectFieldBindingResult() {
		ParameterBindingResult result = new ParameterBindingResult( ( Map<String, Object> ) getTarget(), getObjectName() );
		if ( getConversionService() != null ) {
			result.initConversion( getConversionService() );
		}
		if ( this.messageCodesResolver != null ) {
			result.setMessageCodesResolver( messageCodesResolver );
		}
		return result;
	}

	@Override
	protected AbstractPropertyBindingResult createBeanPropertyBindingResult() {
		BeanBindingResult result = new BeanBindingResult( getTarget(), getObjectName(), isAutoGrowNestedPaths(), getAutoGrowCollectionLimit() );
		if ( getConversionService() != null ) {
			result.initConversion( getConversionService() );
		}
		if ( this.messageCodesResolver != null ) {
			result.setMessageCodesResolver( messageCodesResolver );
		}
		return result;
	}
	
}
