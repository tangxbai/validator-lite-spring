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
package com.viiyue.plugins.validator.spring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.DelegatingMessageSource;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.viiyue.plugins.validator.ValidatorFactory;
import com.viiyue.plugins.validator.handler.Handler;
import com.viiyue.plugins.validator.scripting.configuration.ContextConfigurion;
import com.viiyue.plugins.validator.spring.beans.ValidationRequestMappingHandlerAdapter;
import com.viiyue.plugins.validator.spring.message.SpringMessageResovler;
import com.viiyue.plugins.validator.spring.utils.PrecompileUtils;
import com.viiyue.plugins.validator.utils.BeanUtil;

/**
 * The core bean object of the validation plugin, which replaces some of the
 * default Spring beans.
 *
 * @author tangxbai
 * @since 1.0.0
 * 
 * @see ApplicationListener
 * @see BeanDefinitionRegistryPostProcessor
 */
public class ValidatorLiteBean extends ContextConfigurion implements InitializingBean, ApplicationListener<ContextRefreshedEvent>, BeanDefinitionRegistryPostProcessor {
	
	// Package scanner
	private static final ClassPathScanningCandidateComponentProvider scanner;
	static {
		scanner = new ClassPathScanningCandidateComponentProvider( false );
		scanner.addIncludeFilter( new AssignableTypeFilter( Handler.class ) );
	}
	
	private String handlers;
	private Class<? extends ValidatorFactory> factory;
	
	/**
	 * Custom validation factory implementation
	 * 
	 * @param factory the validation factory implementation
	 */
	public void setFactory( Class<? extends ValidatorFactory> factory ) {
		this.factory = factory;
	}
	
	/**
	 * Scan validation handlers in specified packages, multiple package paths
	 * can be separated by "{@code ,; \t\n}".
	 * 
	 * @param handlers the specified packages
	 */
	public void setHandlers( String handlers ) {
		this.handlers = handlers;
	}

	/**
	 * Instance object of custom validation factory
	 * 
	 * @return the validation factory instance
	 */
	public ValidatorFactory getFactoryInstance() {
		return factory == null ? null : BeanUtil.newInstance( factory );
	}
	
	/**
	 * Get a list of all handler class names in the specified package
	 * 
	 * @return the list of all handler class names
	 */
	public List<String> getHandlerClassNames() {
		if ( StringUtils.isEmpty( handlers ) ) {
			return Collections.emptyList();
		}
		List<String> handlerClassNames = new ArrayList<String>( 32 );
		String [] packages = StringUtils.tokenizeToStringArray( handlers, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS );
		for ( String pattern : packages ) {
			for ( BeanDefinition bean : scanner.findCandidateComponents( pattern )) {
				handlerClassNames.add( bean.getBeanClassName() );
			}
		}
		return handlerClassNames;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// Preparing the operating environment
		com.viiyue.plugins.validator.Validator.prepare();
		
		// Initialize a custom data validation factory implementation class
		// Not initialized if factory class is null
		com.viiyue.plugins.validator.Validator.initFactory( getFactoryInstance() );
		
		// Change preference configuration
		com.viiyue.plugins.validator.Validator.configuration( this, false );
		
		// Registering custom handlers
		ValidatorFactory factory = com.viiyue.plugins.validator.Validator.getFactory();
		for ( String handlerClassName : getHandlerClassNames() ) {
			factory.addHandler( handlerClassName );
		}
	}

	@Override
	public void postProcessBeanDefinitionRegistry( BeanDefinitionRegistry registry ) throws BeansException {
		String handlerAdapterName = RequestMappingHandlerAdapter.class.getName();
		if ( registry.containsBeanDefinition( handlerAdapterName ) ) {
			// Replace spring's default RequestMappingHandlerAdapter object 
			// with ValidationRequestMappingHandlerAdapter with verification function.
			BeanDefinition handlerAdapterDef = registry.getBeanDefinition( handlerAdapterName );
			handlerAdapterDef.setBeanClassName( ValidationRequestMappingHandlerAdapter.class.getName() );
			
			// Inject 'validator-lite' into spring container
			RootBeanDefinition validatorDef = new RootBeanDefinition( com.viiyue.plugins.validator.spring.ValidatorLite.class );
			registry.registerBeanDefinition( "validatorLite", validatorDef );
			
			// Replace Spring's default data validation object
			MutablePropertyValues propertyValues = handlerAdapterDef.getPropertyValues();
			BeanDefinition bindingDef = ( BeanDefinition ) propertyValues.get( "webBindingInitializer" );
			bindingDef.getPropertyValues().add("validator", validatorDef);
		}
	}
	
	@Override
	public void onApplicationEvent( ContextRefreshedEvent event ) {
		// Spring applection context object
		ApplicationContext context = event.getApplicationContext();

		// Validator message resolver
		MessageSource messageSource = context.getBean( MessageSource.class );
		if ( messageSource != null && !( messageSource instanceof DelegatingMessageSource ) ) {
			SpringMessageResovler messageResolver = new SpringMessageResovler( messageSource );
			com.viiyue.plugins.validator.Validator.setMessageResolver( messageResolver );
		}
		
		// Call the initialized function
		com.viiyue.plugins.validator.Validator.getFactory().afterInitialized();
		
		// Precompiled data validation rules
		PrecompileUtils.compile( event.getApplicationContext() );
	}
	
	@Override
	public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory ) throws BeansException {
	}
	
}
