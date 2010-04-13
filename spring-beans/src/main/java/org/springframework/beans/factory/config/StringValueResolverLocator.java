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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

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

	/**
	 * 
	 */
	private static final String STRING_VALUE_RESOLVER_FACTORY_KEY_NAME = StringValueResolverFactory.class
			.getName();

	private final StringValueResolver stringValueResolver;

	/**
	 * The location to look for the mapping files. Can be present in multiple
	 * JAR files.
	 */
	public static final String DEFAULT_BOOTSTRAP_LOCATION = "META-INF/spring.bootstrap";

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** ClassLoader to use for StringValueResolver classes */
	private final ClassLoader classLoader;

	/**
	 * Create a new <code>DefaultStringValueResolver</code> using the default
	 * mapping file location.
	 * <p>
	 * This constructor will result in the thread context ClassLoader being used
	 * to load resources.
	 * 
	 * @see #DEFAULT_BOOTSTRAP_LOCATION
	 */
	public StringValueResolverLocator() {
		this(null, DEFAULT_BOOTSTRAP_LOCATION);
	}

	/**
	 * Create a new <code>DefaultStringValueResolver</code> using the default
	 * mapping file location.
	 * 
	 * @param classLoader
	 *            the {@link ClassLoader} instance used to load mapping
	 *            resources (may be <code>null</code>, in which case the thread
	 *            context ClassLoader will be used)
	 * @see #DEFAULT_BOOTSTRAP_LOCATION
	 */
	public StringValueResolverLocator(ClassLoader classLoader) {
		this(classLoader, DEFAULT_BOOTSTRAP_LOCATION);
	}

	/**
	 * Create a new <code>DefaultStringValueResolver</code> using the supplied
	 * mapping file location.
	 * 
	 * @param classLoader
	 *            the {@link ClassLoader} instance used to load mapping
	 *            resources may be <code>null</code>, in which case the thread
	 *            context ClassLoader will be used)
	 * @param bootstrapLocation
	 *            the mapping file location
	 */
	public StringValueResolverLocator(ClassLoader classLoader,
			String bootstrapLocation) {
		Assert
				.notNull(bootstrapLocation,
						"Bootstrap location must not be null");
		this.classLoader = (classLoader != null ? classLoader : ClassUtils
				.getDefaultClassLoader());
		stringValueResolver = createStringValueResolver(bootstrapLocation);
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

		Collection<Properties> bootstrapMappings = getBootstrapMappings(location);
		CompositeStringValueResolver result = new CompositeStringValueResolver();
		boolean resolved = false;

		for (Properties properties : bootstrapMappings) {

			String className = properties
					.getProperty(STRING_VALUE_RESOLVER_FACTORY_KEY_NAME);
			StringValueResolver resolver;

			if (className != null) {

				try {

					Class<?> handlerClass = ClassUtils.forName(className,
							this.classLoader);

					if (!StringValueResolverFactory.class
							.isAssignableFrom(handlerClass)) {
						throw new FatalBeanException("Class [" + className
								+ "] does not implement the ["
								+ StringValueResolverFactory.class.getName()
								+ "] interface");
					}
					resolver = ((StringValueResolverFactory) BeanUtils
							.instantiateClass(handlerClass))
							.getResolver(classLoader, properties);
					resolved = true;

				} catch (ClassNotFoundException ex) {

					throw new FatalBeanException("StringValueResolver class ["
							+ className + "] not found", ex);

				} catch (LinkageError err) {

					throw new FatalBeanException(
							"Invalid StringValueResolver class ["
									+ className
									+ "]: problem with resolver class file or dependent class",
							err);
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
	 * Load the specified NamespaceHandler mappings lazily.
	 * 
	 * @param location
	 */
	private Collection<Properties> getBootstrapMappings(String location) {

		List<Properties> result = new ArrayList<Properties>();

		Enumeration<URL> urls;
		try {
			urls = classLoader.getResources(location);
		} catch (IOException e) {
			throw new FatalBeanException(
					"Could not get resources from class loader for locations=["
							+ location + "]", e);
		}
		while (urls.hasMoreElements()) {
			URL url = (URL) urls.nextElement();
			InputStream is = null;
			Properties properties = new Properties();
			try {
				URLConnection con = url.openConnection();
				con.setUseCaches(false);
				is = con.getInputStream();
				properties.load(is);
				result.add(properties);
			} catch (IOException e) {
				throw new FatalBeanException(
						"Could not load properties from URL=" + url, e);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						throw new FatalBeanException(
								"Could not close resource from URL=" + url, e);
					}
				}
			}
		}

		return result;

	}
}
