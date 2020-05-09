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
package com.viiyue.plugins.validator.spring.exception;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

/**
 * Validated exception
 * 
 * @author tangxbai
 * @since 1.0.4
 */
public class ValidatedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final BindingResult bindingResult;

	public ValidatedException( BindingResult bindingResult ) {
		this.bindingResult = bindingResult;
	}

	public BindingResult getBindingResult() {
		return bindingResult;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder( "Validation failed for argument with " );
		sb.append( this.bindingResult.getErrorCount() ).append( " error(s): " );
		for ( ObjectError error : this.bindingResult.getAllErrors() ) {
			sb.append( "[" ).append( error ).append( "] " );
		}
		return sb.toString();
	}

}
