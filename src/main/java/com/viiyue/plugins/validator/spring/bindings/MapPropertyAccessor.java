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

import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.lang.Nullable;

/**
 * Common parameters use Map to access parameter properties
 *
 * @author tangxbai
 * @since 1.0.0
 */
class MapPropertyAccessor implements ConfigurablePropertyAccessor {

	private final Map<String, Object> target;
	
	// Added in 1.0.4
	private final Map<String, Class<?>> types;
	private ConversionService conversionService;

	public MapPropertyAccessor( Map<String, Object> target, Map<String, Class<?>> types ) {
		this.target = target;
		this.types = types;
	}

	public void recordValueTypes( String field, Class<?> type ) {
		this.types.put( field, type );
	}
	
	@Override
	public boolean isReadableProperty( String propertyName ) {
		return target.containsKey( propertyName );
	}

	@Override
	public boolean isWritableProperty( String propertyName ) {
		return true;
	}

	@Override
	@Nullable
	public Class<?> getPropertyType( String propertyName ) throws BeansException {
		return types.getOrDefault( propertyName, null );
	}

	@Override
	@Nullable
	public TypeDescriptor getPropertyTypeDescriptor( String propertyName ) throws BeansException {
		throw new IllegalAccessError( "This method is not supported" );
	}

	@Override
	@Nullable
	public Object getPropertyValue( String propertyName ) throws BeansException {
		return this.target.get( propertyName );
	}

	@Override
	public void setPropertyValue( String propertyName, @Nullable Object value ) throws BeansException {
		this.target.put( propertyName, value );
	}

	@Override
	public void setPropertyValue( PropertyValue pv ) throws BeansException {
		this.target.put( pv.getName(), pv.getValue() );
	}

	@Override
	public void setPropertyValues( Map<?, ?> map ) throws BeansException {
		if ( MapUtils.isNotEmpty( map ) ) {
			for ( Entry<?, ?> entry : map.entrySet() ) {
				if ( entry.getKey() != null ) {
					this.target.put( entry.getKey().toString(), entry.getValue() );
				}
			}
		}
	}

	@Override
	public void setPropertyValues( PropertyValues pvs ) throws BeansException {
		for ( PropertyValue pv : pvs.getPropertyValues() ) {
			setPropertyValue( pv );
		}
	}

	@Override
	public void setPropertyValues( PropertyValues pvs, boolean ignoreUnknown ) throws BeansException {
		setPropertyValues( pvs );
	}

	@Override
	public void setPropertyValues( PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid ) throws BeansException {
		setPropertyValues( pvs );
	}

	@Override
	public <T> T convertIfNecessary( @Nullable Object value, @Nullable Class<T> requiredType )
			throws TypeMismatchException {
		return convertIfNecessary( value, null, requiredType );
	}

	@Override
	public <T> T convertIfNecessary( @Nullable Object value, @Nullable Class<T> requiredType,
			@Nullable MethodParameter methodParam ) throws TypeMismatchException {
		return convertIfNecessary( value, methodParam == null ? null : methodParam.getParameterType(), requiredType );
	}

	@Override
	public <T> T convertIfNecessary( @Nullable Object value, @Nullable Class<T> requiredType, @Nullable Field field )
			throws TypeMismatchException {
		return convertIfNecessary( value, field == null ? null : field.getType(), requiredType );
	}

	@Override
	public void setExtractOldValueForEditor( boolean extractOldValueForEditor ) {}

	@Override
	public boolean isExtractOldValueForEditor() {
		return false;
	}

	@Override
	public void setAutoGrowNestedPaths( boolean autoGrowNestedPaths ) {}

	@Override
	public boolean isAutoGrowNestedPaths() {
		return false;
	}
	
	@Override
	public void setConversionService( @Nullable ConversionService conversionService ) {
		this.conversionService = conversionService;
	}

	@Override
	@Nullable
	public ConversionService getConversionService() {
		return conversionService;
	}
	
	@Override
	public void registerCustomEditor( Class<?> requiredType, PropertyEditor propertyEditor ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void registerCustomEditor( @Nullable Class<?> requiredType, @Nullable String propertyPath, PropertyEditor propertyEditor ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PropertyEditor findCustomEditor( @Nullable Class<?> requiredType, @Nullable String propertyPath ) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * <p>
	 * If necessary, use the conversion service data type. If the conversion
	 * service is not configured, {@link ConvertUtils} will be used for parameter
	 * conversion.
	 * 
	 * @param <T> the target data type
	 * @param value the target conversion value
	 * @param sourceType source data type 
	 * @param targetType converted target data type
	 * @return the converted value according to the target data type
	 * 
	 * @see ConvertUtils
	 * @see ConversionService
	 */
	private <T> T convertIfNecessary( @Nullable Object value, @Nullable Class<?> sourceType, @Nullable Class<T> targetType ) {
		if ( value == null ) {
			return null;
		}
		if ( targetType == null ) {
			return ( T ) value;
		}
		if ( sourceType == null ) {
			sourceType = value.getClass();
		}
		if ( conversionService != null && conversionService.canConvert( sourceType, targetType ) ) {
			return conversionService.convert( value, targetType );
		}
		return ConvertUtils.lookup( sourceType, targetType ).convert( targetType, value );
	}

}
