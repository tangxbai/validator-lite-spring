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

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.ServletRequestDataBinderFactory;

/**
 * Wrapped {@link ServletRequestDataBinderFactory}, a binder for adding data validation.
 *
 * @author tangxbai
 * @since 1.0.0
 */
public class ValidationDataBinderFactory extends ServletRequestDataBinderFactory {

	public ValidationDataBinderFactory( 
		List<InvocableHandlerMethod> binderMethods, 
		WebBindingInitializer initializer ) {
		super( binderMethods, initializer );
	}

	@Override
	protected ServletRequestDataBinder createBinderInstance( 
		Object target,
		String objectName,
		NativeWebRequest webRequest ) throws Exception {
		return new ValidationServletRequestDataBinder( target, objectName );
	}

}
