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

import java.util.List;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.InitBinderDataBinderFactory;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

/**
 * Wrapped {@link RequestMappingHandlerAdapter}, used to replace the original
 * {@link RequestMappingHandlerAdapter} in order to add data validation when
 * processing method request parameters.
 *
 * @author tangxbai
 * @since 1.0.0
 */
public class ValidationRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {
		
	@Override
	protected InitBinderDataBinderFactory createDataBinderFactory( List<InvocableHandlerMethod> binderMethods ) throws Exception {
		return new ValidationDataBinderFactory( binderMethods, getWebBindingInitializer() );
	}
	
	@Override
	protected ServletInvocableHandlerMethod createInvocableHandlerMethod( HandlerMethod handlerMethod ) {
		return new ValidationInvocableHandlerMethod( handlerMethod );
	}
	
}