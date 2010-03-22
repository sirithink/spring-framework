/*
 * Copyright 2006-2007 the original author or authors.
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
package org.test.scratch;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringValueResolver;

public class ExampleConfigurationTests {

	private String resource = "classpath:"+ClassUtils.addResourcePathToPackagePath(getClass(), getClass().getSimpleName() + "-context.xml");

	@Before
	public void setUp() {
		// Set up system properties as fall backs.  They should be overriden
		// by the TestStringValueResolver.
		System.setProperty("app", "foo");
		System.setProperty("context", "foo");
		System.setProperty("jdbc.type", "HSQLDB");
		System.setProperty("jdbc.user", "test");
		System.setProperty("remote.url", "http://localhost:8080/service");
	}

	@After
	public void cleanUp() {
		System.clearProperty("app");
		System.clearProperty("context");
		System.clearProperty("jdbc.type");
		System.clearProperty("jdbc.user");
		System.clearProperty("remote.url");
	}

	@Test
	public void testGenericApplicationContext() throws Exception {

		GenericApplicationContext context = new GenericApplicationContext();
		new XmlBeanDefinitionReader(context).loadBeanDefinitions(resource);
		context.refresh();
		Map<String, String> map = getMap(context);
		try {
			// This value was overriden by the placeholder configurer explicitly
			assertEquals("SYBASE", map.get("jdbc.type"));
			assertEquals("test", map.get("jdbc.user"));
			assertEquals("http://localhost:8080/service", map.get("remote.url"));
		}
		finally {
			context.close();
		}

	}

	@Test
	public void testClassPathXmlApplicationContext() throws Exception {

		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(resource);
		Map<String, String> map = getMap(context);
		try {
			assertEquals("SYBASE", map.get("jdbc.type"));
			assertEquals("test", map.get("jdbc.user"));
			assertEquals("http://localhost:8080/service", map.get("remote.url"));
		}
		finally {
			context.close();
		}

	}

	@Test
	public void testClassPathXmlApplicationContextFromLocations() throws Exception {

		String resource = "classpath:"+ClassUtils.addResourcePathToPackagePath(getClass(), getClass().getSimpleName() + "-${context}.xml");
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(resource);
		Map<String, String> map = getMap(context);
		try {
			assertEquals("SYBASE", map.get("jdbc.type"));
			assertEquals("test", map.get("jdbc.user"));
			assertEquals("http://localhost:8080/service", map.get("remote.url"));
		}
		finally {
			context.close();
		}

	}

	/**
	 * Switch the import in the application context to a test location using a
	 * static convenience method in the {@link StringValueResolver} that is used
	 * in the default placeholder resolver.
	 */
	@Test
	public void testSwitchImportLocations() throws Exception {

		// Install the test value of app in the string resolver.  If using @ContextConfiguration 
		// this could be done in @BeforeClass.
		TestStringValueResolver.setProperty("app", "test");

		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(resource);
		Map<String, String> map = getMap(context);
		try {
			assertEquals("TEST", map.get("jdbc.type"));
			assertEquals("testing", map.get("jdbc.user"));
			assertEquals("file://home/service/test-data.xml", map.get("remote.url"));
		}
		finally {
			TestStringValueResolver.remove("app");
			context.close();
		}

	}

	private Map<String, String> getMap(BeanFactory context) {
		Map<String, String> map = context.getBean(Service.class).getMap();
		return map;
	}

	public static class AnotherStringValueResolver implements StringValueResolver {
		public String resolveStringValue(String strVal) {
			if (strVal.equals("jdbc.type")) {
				return "SYBASE";
			}
			return null;
		}
	}

}
