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

import java.util.Locale;

import org.springframework.context.i18n.LocaleContextHolder;

import com.viiyue.plugins.validator.Validator;
import com.viiyue.plugins.validator.scripting.configuration.ContextConfigurion;

/**
 * Language selection tool. If the default language is configured, the default
 * locale will be used first. If it is not configured, the currently used locale
 * will be obtained through Spring.
 *
 * @author tangxbai
 * @since 1.0.0
 * 
 * @see ContextConfigurion
 * @see LocaleContextHolder
 */
public class LocaleUtils {
	
	public static Locale switchLocale() {
		Locale defaultLocale = null;
		ContextConfigurion configuration = Validator.getFactory().getConfiguration();
		if ( configuration == null || ( defaultLocale = configuration.getDefaultLanguage() ) == null ) {
			defaultLocale = LocaleContextHolder.getLocale();
		}
		return defaultLocale;
	}
	
}
