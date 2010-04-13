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
package org.springframework.context.refresh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.refresh.RefreshScopeEvent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ExampleConfigurationTests {

	@Autowired
	private Service service;
	
	@Autowired
	@Qualifier("map")
	private Map<String, Object> map;
	
	@Autowired
	private ApplicationEventPublisher publisher;

	@Test
	public void testSimpleProperties() throws Exception {
		assertEquals("Hello scope!", service.getMessage());
		assertTrue(service instanceof Advised);
	}

	@Test
	public void testRefresh() throws Exception {
		assertEquals("Hello scope!", service.getMessage());
		String id1 = service.toString();
		map.put("message", "Foo");
		publisher.publishEvent(new RefreshScopeEvent(this));
		String id2 = service.toString();
		assertNotSame(id1, id2);
		assertEquals("Foo", service.getMessage());
	}

}
