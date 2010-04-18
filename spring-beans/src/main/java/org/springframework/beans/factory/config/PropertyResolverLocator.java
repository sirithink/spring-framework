/*
 * Copyright 2002-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.springframework.beans.factory.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CompositePropertyResolver;
import org.springframework.util.PropertyResolver;
import org.springframework.util.PropertyResolverFactory;
import org.springframework.util.SystemPropertiesPropertyResolver;

/**
 * @author Dave Syer
 * @since 3.1
 * 
 */
public class PropertyResolverLocator {

	private static final String STRING_VALUE_RESOLVER_FACTORY_KEY_NAME = PropertyResolverFactory.class.getName();

	private final PropertyResolver propertyResolver;

	private static Map<String, PropertyResolver> inProgressDefaults = new HashMap<String, PropertyResolver>();

	private static Map<String, AtomicInteger> inProgressKeys = new HashMap<String, AtomicInteger>();

	/**
	 * The location to look for the mapping files. Can be present in multiple JAR files.
	 */
	public static final String DEFAULT_BOOTSTRAP_LOCATION = "META-INF/spring.bootstrap";

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** ClassLoader to use for StringValueResolver classes */
	private final ClassLoader classLoader;

	/**
	 * Create a new <code>DefaultStringValueResolver</code> using the default mapping file location.
	 * 
	 * @param classLoader the {@link ClassLoader} instance used to load mapping resources (may be <code>null</code>, in
	 * which case the thread context ClassLoader will be used)
	 * @see #DEFAULT_BOOTSTRAP_LOCATION
	 */
	public static PropertyResolver locate(ClassLoader classLoader) {
		return new PropertyResolverLocator(classLoader, DEFAULT_BOOTSTRAP_LOCATION).getStringValueResolver();
	}

	/**
	 * Create a new <code>PropertyResolver</code> using the default mapping file location.
	 * 
	 * @param classLoader the {@link ClassLoader} instance used to load mapping resources (may be <code>null</code>, in
	 * which case the thread context ClassLoader will be used)
	 * @param bootstrapLocation the location of the bootstrap properties
	 */
	public static PropertyResolver locate(ClassLoader classLoader, String bootstrapLocation) {
		return new PropertyResolverLocator(classLoader, bootstrapLocation).getStringValueResolver();
	}

	private PropertyResolverLocator(ClassLoader classLoader, String bootstrapLocation) {

		Assert.notNull(classLoader, "The ClassLoader must not be null");

		this.classLoader = classLoader;
		synchronized (inProgressKeys) {
			if (isInProgress(classLoader, bootstrapLocation)) {
				logger.info("Using fallback PropertyResolver for location=" + bootstrapLocation);
				propertyResolver = getInProgressResolver(classLoader, bootstrapLocation);
			}
			else {
				try {
					logger.info("Creating PropertyResolverFactory from location=" + bootstrapLocation);
					setInProgress(classLoader, bootstrapLocation);
					propertyResolver = createPropertyResolver(bootstrapLocation);
				}
				finally {
					resetInProgress(classLoader, bootstrapLocation);
				}
			}
		}
	}

	private void resetInProgress(ClassLoader classLoader, String bootstrapLocation) {
		logger.info("Reset fallback PropertyResolverFactory for location=" + bootstrapLocation);
		String key = getKey(classLoader, bootstrapLocation);
		if (inProgressKeys.get(key).decrementAndGet() <= 0) {
			inProgressKeys.remove(key);
			inProgressDefaults.remove(key);
		}
	}

	private void setInProgress(ClassLoader classLoader, String bootstrapLocation) {

		String key = getKey(classLoader, bootstrapLocation);
		if (!inProgressKeys.containsKey(key)) {
			inProgressKeys.put(key, new AtomicInteger(1));
		}
		else {
			inProgressKeys.get(key).incrementAndGet();
		}
		logger.debug("Registered fallback number "+inProgressKeys.get(key).get()+" for location=" + bootstrapLocation);

	}

	private PropertyResolver getInProgressResolver(ClassLoader classLoader, String bootstrapLocation) {
		String key = getKey(classLoader, bootstrapLocation);
		if (!inProgressDefaults.containsKey(key)) {
			PropertyResolver resolver;
			String defaultKey = getKey(classLoader, DEFAULT_BOOTSTRAP_LOCATION);
			if (!inProgressKeys.containsKey(defaultKey)) {
				logger.debug("Creating fallback PropertyResolverFactory from location=" + DEFAULT_BOOTSTRAP_LOCATION);
				resolver = PropertyResolverLocator.locate(classLoader, DEFAULT_BOOTSTRAP_LOCATION);
			}
			else {
				logger.debug("Creating fallback to SystemPropertyPropertyResolver");
				resolver = new SystemPropertiesPropertyResolver();
			}
			inProgressDefaults.put(key, resolver);
		}
		return inProgressDefaults.get(key);
	}

	private boolean isInProgress(ClassLoader classLoader, String bootstrapLocation) {
		return inProgressKeys.containsKey(getKey(classLoader, bootstrapLocation));
	}

	private String getKey(ClassLoader classLoader, String bootstrapLocation) {
		return classLoader.toString() + ":" + bootstrapLocation;
	}

	/**
	 * @return the stringValueResolver
	 */
	public PropertyResolver getStringValueResolver() {
		return propertyResolver;
	}

	/**
	 * @param location
	 * @return
	 */
	private PropertyResolver createPropertyResolver(String location) {

		Collection<Properties> bootstrapMappings = getBootstrapMappings(location);
		CompositePropertyResolver result = new CompositePropertyResolver();
		PropertyResolver resolver = null;

		for (Properties properties : bootstrapMappings) {

			String className = properties.getProperty(STRING_VALUE_RESOLVER_FACTORY_KEY_NAME);

			if (className != null) {

				if (logger.isDebugEnabled()) {
					logger.debug("Found PropertyResolverFactory class name: " + className);
				}

				try {

					Class<?> handlerClass = ClassUtils.forName(className, this.classLoader);

					if (!PropertyResolverFactory.class.isAssignableFrom(handlerClass)) {
						throw new FatalBeanException("Class [" + className + "] does not implement the ["
								+ PropertyResolverFactory.class.getName() + "] interface");
					}
					resolver = ((PropertyResolverFactory) BeanUtils.instantiateClass(handlerClass)).getResolver(
							classLoader, properties);
					logger.info("Created PropertyResolver from factory with class name: " + className);

				}
				catch (ClassNotFoundException ex) {

					throw new FatalBeanException("PropertyResolver class [" + className + "] not found", ex);

				}
				catch (LinkageError err) {

					throw new FatalBeanException("Invalid PropertyResolver class [" + className
							+ "]: problem with resolver class file or dependent class", err);
				}

				result.addPropertyResolver(resolver);

			}

		}

		if (resolver==null) {
			logger.info("No resolvers located so using SystemPropertiesPropertyResolver as default for location="+location);
			return new SystemPropertiesPropertyResolver();
		}
		
		if (result.size()==1) {
			return resolver;
		}

		return result;

	}

	/**
	 * Load the specified string resolver mappings lazily.
	 * 
	 * @param location
	 */
	private Collection<Properties> getBootstrapMappings(String location) {

		List<Properties> result = new ArrayList<Properties>();

		Enumeration<URL> urls;
		try {
			urls = classLoader.getResources(location);
		}
		catch (IOException e) {
			throw new FatalBeanException("Could not get resources from class loader for locations=[" + location + "]",
					e);
		}
		while (urls.hasMoreElements()) {
			URL url = (URL) urls.nextElement();
			InputStream is = null;
			Properties properties = new Properties();
			try {
				URLConnection con = url.openConnection();
				if (logger.isDebugEnabled()) {
					logger.debug("Loading bootstrap properties from: " + url);
				}
				con.setUseCaches(false);
				is = con.getInputStream();
				properties.load(is);
				result.add(properties);
			}
			catch (IOException e) {
				throw new FatalBeanException("Could not load properties from URL=" + url, e);
			}
			finally {
				if (is != null) {
					try {
						is.close();
					}
					catch (IOException e) {
						throw new FatalBeanException("Could not close resource from URL=" + url, e);
					}
				}
			}
		}

		return result;

	}
}
