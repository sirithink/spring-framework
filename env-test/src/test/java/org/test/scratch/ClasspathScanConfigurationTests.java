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
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.ClassUtils;

public class ClasspathScanConfigurationTests {

	private String resource = "classpath:"
			+ ClassUtils.addResourcePathToPackagePath(getClass(), getClass().getSimpleName() + "-context.xml");

	@Before
	public void setUp() {
		System.setProperty("scratch", "foo");
		System.setProperty("jdbc.type", "HSQLDB");
		System.setProperty("jdbc.user", "test");
		System.setProperty("remote.url", "http://localhost:8080/service");
	}

	@After
	public void cleanUp() {
		System.clearProperty("scratch");
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
			assertEquals("ORACLE", map.get("jdbc.type"));
			assertEquals("test", map.get("jdbc.user"));
			assertEquals("http://localhost:8080/service", map.get("remote.url"));
		}
		finally {
			context.close();
		}

	}

	@Test
	public void testAnnotationConfigApplicationContext() throws Exception {

		/**
		 * This works because we tweaked Spring to resolve placeholders in
		 * annotations even without a placeholder configurer in the bean factory
		 */
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("org.test.${scratch}");
		Map<String, String> map = getMap(context);
		try {
			assertEquals("ORACLE", map.get("jdbc.type"));
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
			assertEquals("ORACLE", map.get("jdbc.type"));
			assertEquals("test", map.get("jdbc.user"));
			assertEquals("http://localhost:8080/service", map.get("remote.url"));
		}
		finally {
			context.close();
		}

	}

	private Map<String, String> getMap(BeanFactory context) {
		Map<String, String> map = context.getBean(Service.class).getMap();
		return map;
	}

}
