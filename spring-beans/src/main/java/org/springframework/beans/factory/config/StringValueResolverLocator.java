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
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CompositeStringValueResolver;
import org.springframework.util.StringValueResolver;
import org.springframework.util.StringValueResolverFactory;
import org.springframework.util.SystemPropertyStringValueResolver;

/**
 * @author Dave Syer
 * @since 3.1
 * 
 */
public class StringValueResolverLocator {

	private static final String STRING_VALUE_RESOLVER_FACTORY_KEY_NAME = StringValueResolverFactory.class.getName();

	private final StringValueResolver stringValueResolver;

	private static Map<ClassLoader, Stack<StringValueResolverLocator>> inProgressHolder = new HashMap<ClassLoader, Stack<StringValueResolverLocator>>();

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
	public static StringValueResolver locate(ClassLoader classLoader) {
		return new StringValueResolverLocator(classLoader, DEFAULT_BOOTSTRAP_LOCATION).getStringValueResolver();
	}

	/**
	 * Create a new <code>DefaultStringValueResolver</code> using the default mapping file location.
	 * 
	 * @param classLoader the {@link ClassLoader} instance used to load mapping resources (may be <code>null</code>, in
	 * which case the thread context ClassLoader will be used)
	 * @param bootstrapLocation the location of the bootstrap properties
	 */
	public static StringValueResolver locate(ClassLoader classLoader, String bootstrapLocation) {
		return new StringValueResolverLocator(classLoader, bootstrapLocation).getStringValueResolver();
	}

	private StringValueResolverLocator(ClassLoader classLoader, String bootstrapLocation) {

		Assert.notNull(classLoader, "The ClassLoader must not be null");

		this.classLoader = classLoader;
		synchronized (inProgressHolder) {
			if (isInProgress(classLoader)) {
				logger.info("Using existing StringValueResolver already in progress for this class loader");
				stringValueResolver = getInProgressResolver(classLoader);
			}
			else {
				try {
					logger.info("Locating StringValueResolver from location=" + bootstrapLocation);
					setInProgress(classLoader);
					stringValueResolver = createStringValueResolver(bootstrapLocation);
				}
				finally {
					resetInProgress(classLoader);
				}
			}
		}
	}

	private void resetInProgress(ClassLoader bootstrapLocation) {
		if (isInProgress(bootstrapLocation)) {
			Stack<StringValueResolverLocator> stack = inProgressHolder.get(bootstrapLocation);
			stack.pop();
			if (stack.isEmpty()) {
				inProgressHolder.remove(bootstrapLocation);
			}
		}
	}

	private void setInProgress(ClassLoader bootstrapLocation) {
		if (!inProgressHolder.containsKey(bootstrapLocation)) {
			inProgressHolder.put(bootstrapLocation, new Stack<StringValueResolverLocator>());
		}
		inProgressHolder.get(bootstrapLocation).add(this);
	}

	private StringValueResolver getInProgressResolver(ClassLoader bootstrapLocation) {
		return inProgressHolder.get(bootstrapLocation).peek().getStringValueResolver();
	}

	private boolean isInProgress(ClassLoader bootstrapLocation) {
		return inProgressHolder.containsKey(bootstrapLocation);
	}

	/**
	 * @return the stringValueResolver
	 */
	public StringValueResolver getStringValueResolver() {
		return stringValueResolver;
	}

	/**
	 * @param location
	 * @return
	 */
	private StringValueResolver createStringValueResolver(String location) {

		logger.info("Creating StringValueResolver from location=" + location);

		Collection<Properties> bootstrapMappings = getBootstrapMappings(location);
		CompositeStringValueResolver result = new CompositeStringValueResolver();
		boolean resolved = false;

		for (Properties properties : bootstrapMappings) {

			String className = properties.getProperty(STRING_VALUE_RESOLVER_FACTORY_KEY_NAME);
			StringValueResolver resolver;

			if (className != null) {

				if (logger.isDebugEnabled()) {
					logger.debug("Found StringValueResolverFactory class name: " + className);
				}

				try {

					Class<?> handlerClass = ClassUtils.forName(className, this.classLoader);

					if (!StringValueResolverFactory.class.isAssignableFrom(handlerClass)) {
						throw new FatalBeanException("Class [" + className + "] does not implement the ["
								+ StringValueResolverFactory.class.getName() + "] interface");
					}
					resolver = ((StringValueResolverFactory) BeanUtils.instantiateClass(handlerClass)).getResolver(
							classLoader, properties);
					resolved = true;

				}
				catch (ClassNotFoundException ex) {

					throw new FatalBeanException("StringValueResolver class [" + className + "] not found", ex);

				}
				catch (LinkageError err) {

					throw new FatalBeanException("Invalid StringValueResolver class [" + className
							+ "]: problem with resolver class file or dependent class", err);
				}

				result.addStringValueResolver(resolver);

			}

		}

		if (!resolved) {
			return new SystemPropertyStringValueResolver();
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
