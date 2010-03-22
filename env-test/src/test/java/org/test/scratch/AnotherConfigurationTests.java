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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class AnotherConfigurationTests {
	
	@Autowired
	private Service service;

	@BeforeClass
	public static void setUp() {
		System.setProperty("app", "foo");
		System.setProperty("jdbc.type", "HSQLDB");
		System.setProperty("jdbc.user", "test");
		System.setProperty("remote.url", "http://localhost:8080/service");
	}

	@AfterClass
	public static void cleanUp() {
		System.clearProperty("app");
		System.clearProperty("jdbc.type");
		System.clearProperty("jdbc.user");
		System.clearProperty("remote.url");
	}

	@Test
	public void testSimpleProperties() throws Exception {
		Map<String, String> map = service.getMap();
		assertEquals("ORACLE", map.get("jdbc.type"));
		assertEquals("test", map.get("jdbc.user"));
		assertEquals("http://localhost:8080/service", map.get("remote.url"));
	}
	
}
