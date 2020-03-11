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
package com.viiyue.plugins.validator.spring.utils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.viiyue.plugins.validator.Validator;
import com.viiyue.plugins.validator.utils.ArrayUtil;

/**
 * Validation rule precompilation tool class
 *
 * @author tangxbai
 * @since 1.0.0
 */
public class PrecompileUtils {
	
	private static final Logger log = LoggerFactory.getLogger( Validator.class );
	private static final String beanName = "requestMappingHandlerMapping";

	public static void compile( ApplicationContext context ) {
		if ( context.containsBean( beanName ) ) {
			long startTime = System.currentTimeMillis();
			// Precompile method parameter validation annotations in the controller
			// to reduce time consumption during actual validation
			RequestMappingHandlerMapping handlerMapping = context.getBean( RequestMappingHandlerMapping.class );
			Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
			for ( HandlerMethod handlerMethod : handlerMethods.values() ) {
				MethodParameter [] parameters = handlerMethod.getMethodParameters();
				if ( ArrayUtil.isNotEmpty( parameters ) ) {
					Validated validated = handlerMethod.getMethodAnnotation( Validated.class );
					// Check every parameter of the controller, 
					// and pre-compiled the rules if the @Validated annotation is marked.
					for ( MethodParameter methodParameter : parameters ) {
						if ( validated != null || methodParameter.hasParameterAnnotation( Validated.class ) ) {
							if ( Validator.compile( methodParameter.getParameter() ) == null ) { // Ordinary parameters
								Validator.compile( methodParameter.getParameterType() ); // Entity bean object
							}
						}
					}
				}
			}
			log.info( "Pre-compilation of validation rules is completed, processing time {}ms", System.currentTimeMillis() - startTime );
		}
	}
	
}
