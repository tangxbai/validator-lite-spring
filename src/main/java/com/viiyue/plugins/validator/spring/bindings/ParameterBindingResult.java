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

import java.util.Map;

import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.lang.Nullable;
import org.springframework.validation.AbstractPropertyBindingResult;

import com.viiyue.plugins.validator.metadata.result.ValidatedResult;

/**
 * Wrapped {@link AbstractPropertyBindingResult}, used to encapsulate basic
 * parameter binding objects.
 *
 * @author tangxbai
 * @since 1.0.0
 * @see AbstractPropertyBindingResult 
 */
public class ParameterBindingResult extends AbstractPropertyBindingResult {

	private static final long serialVersionUID = 1L;

	private final Map<String, Object> target;
	private ValidatedResult validated;

	public ParameterBindingResult( @Nullable Map<String, Object> target, String objectName ) {
		super( objectName );
		this.target = target;
	}

	@Override
	public ConfigurablePropertyAccessor getPropertyAccessor() {
		return new MapPropertyAccessor( target );
	}

	@Override
	public Object getTarget() {
		return target;
	}

	public ValidatedResult getValidated() {
		return validated;
	}

	public void setValidated( ValidatedResult validated ) {
		if ( this.validated == null ) {
			this.validated = validated;
		}
	}
	
	public void putParameter( String field, @Nullable Object value, Class<?> type ) {
		if ( this.target != null ) {
			this.target.put( field, value );
			this.recordFieldValue( field, type, value );
		}
	}

}
