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

package org.test.scratch;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.PropertyResolverLocator;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ClassUtils;
import org.springframework.util.PlaceholderResolvingStringValueResolver;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyResolver;
import org.springframework.util.PropertyResolverFactory;

/**
 * @author Dave Syer
 * 
 */
public class PropertyResolverLocatorTests {

	private Level locatorLevel = null;

	private Level placeholderLevel = null;

	public static Log logger = LogFactory.getLog(PropertyResolverLocatorTests.class);

	@Before
	public void setUp() {
		Logger logger = LogManager.getLogger(ClassUtils.getPackageName(PropertyResolverLocator.class));
		locatorLevel = logger.getLevel();
		logger.setLevel(Level.DEBUG);
		logger = LogManager.getLogger(PropertyPlaceholderHelper.class);
		placeholderLevel = logger.getLevel();
		logger.setLevel(Level.TRACE);
		System.setProperty("scratch", "foo");
	}

	@After
	public void cleanUp() {
		if (locatorLevel != null) {
			LogManager.getLogger(ClassUtils.getPackageName(PropertyResolverLocator.class)).setLevel(locatorLevel);
			LogManager.getLogger(PropertyPlaceholderHelper.class).setLevel(placeholderLevel);
		}
		System.clearProperty("scratch");
		StringValues.remove("scratch");
	}

	@Test
	public void testBootstrapFromXml() throws Exception {
		StringValues.setProperty("scratch", "bar");
		PropertyResolver resolver = PropertyResolverLocator.locate(ClassUtils.getDefaultClassLoader(),
				"META-INF/test.bootstrap");
		assertEquals("bar", resolver.resolveStringValue("scratch"));
	}

	@Test
	public void testBootstrapFromXmlWithOverride() throws Exception {
		StringValues.setProperty("scratch", "bar");
		PropertyResolver resolver = PropertyResolverLocator.locate(ClassUtils.getDefaultClassLoader(),
				"META-INF/override.bootstrap");
		// The StringValueResolver comes out of override-context.xml which has foo=${scratch}, but before that
		// was loaded the test.bootstrap was set up as the default resolver, and it has scratch=bar
		assertEquals("bar", resolver.resolveStringValue("foo"));
	}

	public static class XmlPropertyResolverFactory implements PropertyResolverFactory {

		public PropertyResolver getResolver(ClassLoader classLoader, Properties properties) {
			ApplicationContext context = new ClassPathXmlApplicationContext("bootstrap-context.xml", getClass());
			return context.getBean(PropertyResolver.class);
		}

	}

	public static class OverrideXmlPropertyResolverFactory implements PropertyResolverFactory {

		public PropertyResolver getResolver(ClassLoader classLoader, Properties properties) {

			PropertyResolver resolver = PropertyResolverLocator.locate(classLoader, "META-INF/test.bootstrap");
			assertEquals("bar", resolver.resolveStringValue("scratch"));

			GenericApplicationContext context = new GenericApplicationContext();
			PlaceholderResolvingStringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver(new PropertyPlaceholderHelper(true), resolver);
			valueResolver.setOrder(0);
			context.getBeanFactory().addEmbeddedValueResolver(valueResolver);

			XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
			reader.loadBeanDefinitions(new ClassPathResource("override-context.xml", getClass()));

			try {
				context.refresh();
				return context.getBean(PropertyResolver.class);
			}
			catch (RuntimeException e) {
				throw e;
			}
			finally {
				try {
					context.close();
				}
				catch (Exception e) {
					logger.error(e);
				}
			}

		}
	}

	public static class StringValues implements PropertyResolver {

		private static Properties properties = new Properties();

		static public void setProperty(String key, String value) {
			properties.setProperty(key, value);
		}

		static public void remove(String key) {
			properties.remove(key);
		}

		public String resolveStringValue(String strVal) {
			if (properties.containsKey(strVal)) {
				return properties.getProperty(strVal);
			}
			return null;
		}

	}

}
