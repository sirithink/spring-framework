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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.StringValueResolverLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringValueResolver;
import org.springframework.util.StringValueResolverFactory;

/**
 * @author Dave Syer
 * 
 */
public class StringValueResolverLocatorTests {

	@Before
	public void setUp() {
		System.setProperty("scratch", "foo");
	}

	@After
	public void cleanUp() {
		System.clearProperty("scratch");
		TestStringValueResolver.remove("scratch");
	}

	@Test
	public void testBootstrapFromXml() throws Exception {
		TestStringValueResolver.setProperty("scratch", "bar");
		StringValueResolver resolver = StringValueResolverLocator.locate(ClassUtils.getDefaultClassLoader(),
				"META-INF/test.bootstrap");
		assertEquals("bar", resolver.resolveStringValue("scratch"));
	}

	public static class XmlStringValueResolverFactory implements StringValueResolverFactory {

		public StringValueResolver getResolver(ClassLoader classLoader, Properties properties) {
			ApplicationContext context = new ClassPathXmlApplicationContext("bootstrap-context.xml", getClass());
			return context.getBean(StringValueResolver.class);
		}

	}

}
