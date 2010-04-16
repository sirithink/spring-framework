/*
 * Copyright 2002-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.util.CompositeStringValueResolver;
import org.springframework.util.PlaceholderResolvingStringValueResolver;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringValueResolver;

/**
 * A property resource configurer that resolves placeholders in bean property
 * values of context definitions. It <i>pulls</i> values from a
 * StringValueResolver into bean definitions.
 * 
 * <p>
 * The default placeholder syntax follows the Ant / Log4J / JSP EL style:
 * 
 * <pre class="code">
 * ${...}
 * </pre>
 * 
 * Example XML context definition:
 * 
 * <pre class="code">
 * &lt;bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource"&gt;
 *   &lt;property name="driverClassName"&gt;&lt;value&gt;${driver}&lt;/value&gt;&lt;/property&gt;
 *   &lt;property name="url"&gt;&lt;value&gt;jdbc:${dbname}&lt;/value&gt;&lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * Example properties file:
 * 
 * <pre class="code">
 * driver=com.mysql.jdbc.Driver
 * dbname=mysql:mydb
 * </pre>
 * 
 * StringValueResolverPlaceholderConfigurer checks simple property values,
 * lists, maps, props, and bean names in bean references. Furthermore,
 * placeholder values can also cross-reference other placeholders, like:
 * 
 * <pre class="code">
 * rootPath=myrootdir
 * subPath=${rootPath}/subdir
 * </pre>
 * 
 * <p>
 * If a configurer cannot resolve a placeholder, a BeanDefinitionStoreException
 * will be thrown. If you want to check against multiple resolvers, specify them
 * using a {@link CompositeStringValueResolver}. You can also define multiple
 * StringValueResolverPlaceholderConfigurers, each with its <i>own</i>
 * placeholder syntax.
 * 
 * <p>
 * Note that the context definition <i>is</i> aware of being incomplete; this is
 * immediately obvious to users when looking at the XML definition file. Hence,
 * placeholders have to be resolved; any desired defaults have to be defined as
 * placeholder values as well (for example in a default properties file).
 * 
 * @author Juergen Hoeller
 * @author Dave Syer
 * @since 3.1
 * @see #setStringValueResolver
 * @see #setPlaceholderPrefix
 * @see #setPlaceholderSuffix
 * @see PropertyPlaceholderConfigurer
 */
public class StringValueResolverPlaceholderConfigurer implements
		BeanFactoryPostProcessor, BeanNameAware, BeanFactoryAware {

	private String placeholderPrefix = PropertyPlaceholderHelper.DEFAULT_PLACEHOLDER_PREFIX;

	private String placeholderSuffix = PropertyPlaceholderHelper.DEFAULT_PLACEHOLDER_SUFFIX;

	private String valueSeparator = PropertyPlaceholderHelper.DEFAULT_VALUE_SEPARATOR;

	private boolean ignoreUnresolvablePlaceholders = false;

	private String nullValue;

	private String beanName;

	private BeanFactory beanFactory;

	private StringValueResolver stringValueResolver;

	/**
	 * Set the prefix that a placeholder string starts with. The default is
	 * "${".
	 * 
	 * @see #DEFAULT_PLACEHOLDER_PREFIX
	 */
	public void setPlaceholderPrefix(String placeholderPrefix) {
		this.placeholderPrefix = placeholderPrefix;
	}

	/**
	 * Set the suffix that a placeholder string ends with. The default is "}".
	 * 
	 * @see #DEFAULT_PLACEHOLDER_SUFFIX
	 */
	public void setPlaceholderSuffix(String placeholderSuffix) {
		this.placeholderSuffix = placeholderSuffix;
	}

	/**
	 * Set whether to ignore unresolvable placeholders. Default is "false": An
	 * exception will be thrown if a placeholder cannot be resolved.
	 */
	public void setIgnoreUnresolvablePlaceholders(
			boolean ignoreUnresolvablePlaceholders) {
		this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
	}

	/**
	 * Set a value that should be treated as <code>null</code> when resolved as
	 * a placeholder value: e.g. "" (empty String) or "null".
	 * <p>
	 * Note that this will only apply to full property values, not to parts of
	 * concatenated values.
	 * <p>
	 * By default, no such null value is defined. This means that there is no
	 * way to express <code>null</code> as a property value unless you explictly
	 * map a corresponding value here.
	 */
	public void setNullValue(String nullValue) {
		this.nullValue = nullValue;
	}

	/**
	 * Only necessary to check that we're not parsing our own bean definition,
	 * to avoid failing on unresolvable placeholders in properties file
	 * locations. The latter case can happen with placeholders for system
	 * properties in resource locations.
	 * 
	 * @see #setLocations
	 * @see org.springframework.core.io.ResourceEditor
	 */
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	/**
	 * Only necessary to check that we're not parsing our own bean definition,
	 * to avoid failing on unresolvable placeholders in properties file
	 * locations. The latter case can happen with placeholders for system
	 * properties in resource locations.
	 * 
	 * @see #setLocations
	 * @see org.springframework.core.io.ResourceEditor
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Set the StringValueResolver to use for resolving placeholders.
	 */
	public void setStringValueResolver(StringValueResolver stringValueResolver) {
		this.stringValueResolver = stringValueResolver;
	}

	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {

		PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper(
				placeholderPrefix, placeholderSuffix, valueSeparator,
				ignoreUnresolvablePlaceholders, nullValue);

		if (stringValueResolver == null) {
			if (beanFactory.containsBean("stringValueResolver")) {
				stringValueResolver = beanFactory.getBean(
						"stringValueResolver", StringValueResolver.class);
			} else {
				stringValueResolver = StringValueResolverLocator
						.locate(beanFactory.getBeanClassLoader());
			}
		}

		StringValueResolver stringValueResolver = new PlaceholderResolvingStringValueResolver(
				propertyPlaceholderHelper, this.stringValueResolver);

		BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(
				stringValueResolver);

		String[] beanNames = beanFactory.getBeanDefinitionNames();
		for (String curName : beanNames) {
			// Check that we're not parsing our own bean definition,
			// to avoid failing on unresolvable placeholders.
			if (!(curName.equals(this.beanName) && beanFactory
					.equals(this.beanFactory))) {
				BeanDefinition bd = beanFactory.getBeanDefinition(curName);
				try {
					visitor.visitBeanDefinition(bd);
				} catch (Exception ex) {
					throw new BeanDefinitionStoreException(bd
							.getResourceDescription(), curName, ex.getMessage());
				}
			}
		}

		// New in Spring 2.5: resolve placeholders in alias target names and
		// aliases as well.
		beanFactory.resolveAliases(stringValueResolver);

		// New in Spring 3.0: resolve placeholders in embedded values such as
		// annotation attributes.
		beanFactory.addEmbeddedValueResolver(stringValueResolver);
	}

}
