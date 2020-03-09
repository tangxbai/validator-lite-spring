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
package com.viiyue.plugins.validator.spring.bindings;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.validation.BeanPropertyBindingResult;

import com.viiyue.plugins.validator.metadata.result.ValidatedResult;

/**
 * Wrapped {@link BeanPropertyBindingResult}, used instead of Bean validation
 * binding object.
 *
 * @author tangxbai
 * @since 1.0.0
 * @see BeanPropertyBindingResult
 */
public class BeanBindingResult extends BeanPropertyBindingResult {

	private static final long serialVersionUID = 1L;

	private ValidatedResult validated;

	public BeanBindingResult( 
		@Nullable Object target, 
		String objectName, 
		boolean autoGrowNestedPaths, 
		int autoGrowCollectionLimit ) {
		super( target, objectName, autoGrowNestedPaths, autoGrowCollectionLimit );
	}

	public ValidatedResult getValidated() {
		return validated;
	}

	public void setValidated( @NonNull ValidatedResult validated ) {
		this.validated = validated;
	}

}
