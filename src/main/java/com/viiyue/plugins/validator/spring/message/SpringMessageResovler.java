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
package com.viiyue.plugins.validator.spring.message;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.support.AbstractResourceBasedMessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.lang.Nullable;

import com.viiyue.plugins.validator.common.Constants;
import com.viiyue.plugins.validator.scripting.configuration.ContextConfigurion;
import com.viiyue.plugins.validator.scripting.configuration.MessageResource;
import com.viiyue.plugins.validator.scripting.message.DefaultMessageResolver;
import com.viiyue.plugins.validator.scripting.message.MessageResolver;
import com.viiyue.plugins.validator.utils.Assert;

/**
 * Validator message resolver. In the spring environment, instead of using the
 * default parser, use Spring's message resolver to process internationalized text.
 *
 * @author tangxbai
 * @since 1.0.0
 * 
 * @see MessageSource
 * @see DefaultMessageResolver
 */
public class SpringMessageResovler implements MessageResolver {
	
	private final String keyPrefix;
	private final MessageSource messageSource;
	private Locale defaultLocale;
	private ContextConfigurion configuration;
	
	public SpringMessageResovler( MessageSource messageSource ) {
		this.messageSource = messageSource;
		this.keyPrefix = Constants.DEFAULT_MESSAGE_KEY_PREFIX + ".";
	}

	@Override
	public Locale getDefaultLocale() {
		if ( defaultLocale == null ) {
			this.defaultLocale = Locale.getDefault();
		}
		return defaultLocale;
	}
	
	@Override
	public void setDefaultLocale( Locale defaultLocale ) {
		this.defaultLocale = defaultLocale;
	}
	
	@Override
	public ContextConfigurion getConfiguration() {
		return configuration;
	}
	
	@Override
	public void setConfiguration( ContextConfigurion configuration ) {
		this.configuration = configuration;
	}
	
	@Override
	public String getMessageKey( String key ) {
		Assert.notNull( key, "Resource key cannot be null" );
		return keyPrefix == null ? key : keyPrefix.concat( key );
	}
	
	@Override
	public void addResourceBundle( String resourceName, @Nullable String ... preloadings ) {
		if ( messageSource != null && messageSource instanceof AbstractResourceBasedMessageSource ) {
			( ( AbstractResourceBasedMessageSource ) messageSource ).addBasenames( getResourceName( resourceName ) );
		}
	}

	@Override
	public void addResourceBundles( List<MessageResource> resources ) {
		if ( messageSource != null && messageSource instanceof AbstractResourceBasedMessageSource ) {
			AbstractResourceBasedMessageSource rbms = ( ( AbstractResourceBasedMessageSource ) messageSource );
			for ( MessageResource messageResource : resources ) {
				rbms.addBasenames( getResourceName( messageResource.getBaseName() ) );
			}
		}
	}

	@Override
	public Set<ResourceBundle> getResourceBundles() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String resolve( String key ) {
		return resolve( key, null, null );
	}

	@Override
	public String resolve( String key, @Nullable String defaultValue ) {
		return resolve( key, null, defaultValue );
	}

	@Override
	public String resolve( String key, @Nullable Locale locale ) {
		return resolve( key, locale, null );
	}

	@Override
	public String resolve( String key, @Nullable Locale locale, @Nullable String defaultValue ) {
		return messageSource.getMessage( key, ArrayUtils.EMPTY_OBJECT_ARRAY, defaultValue, ObjectUtils.defaultIfNull( locale, getDefaultLocale() ) );
	}
	
	public String getResourceName( String resourceName ) {
		if ( messageSource instanceof ReloadableResourceBundleMessageSource ) {
			return "classpath:" + StringUtils.replace( resourceName, ".", "/" );
		}
		return resourceName;
	}

}
