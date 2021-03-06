/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.orm.jpa.support;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Costin Leau
 * @author Juergen Hoeller
 * @author Chris Beams
 */
@Ignore // dependency issues after moving from .testsuite -> .test
public class OpenEntityManagerInViewTests {
    
//	private MockControl factoryControl, managerControl;
//
//	private EntityManager manager;
//
//	private EntityManagerFactory factory;
//
//	private JpaTemplate template;
//
//
//	@Override
//	protected void setUp() throws Exception {
//		factoryControl = MockControl.createControl(EntityManagerFactory.class);
//		factory = (EntityManagerFactory) factoryControl.getMock();
//		managerControl = MockControl.createControl(EntityManager.class);
//		manager = (EntityManager) managerControl.getMock();
//
//		template = new JpaTemplate(factory);
//		template.afterPropertiesSet();
//
//		factoryControl.expectAndReturn(factory.createEntityManager(), manager);
//	}
//
//	@Override
//	protected void tearDown() throws Exception {
//		assertTrue(TransactionSynchronizationManager.getResourceMap().isEmpty());
//		assertFalse(TransactionSynchronizationManager.isSynchronizationActive());
//		assertFalse(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
//		assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
//	}
//
//	public void testOpenEntityManagerInterceptorInView() throws Exception {
//		OpenEntityManagerInViewInterceptor rawInterceptor = new OpenEntityManagerInViewInterceptor();
//		rawInterceptor.setEntityManagerFactory(factory);
//		HandlerInterceptor interceptor = new WebRequestHandlerInterceptorAdapter(rawInterceptor);
//
//		MockServletContext sc = new MockServletContext();
//		MockHttpServletRequest request = new MockHttpServletRequest(sc);
//		MockHttpServletResponse response = new MockHttpServletResponse();
//		
//		managerControl.replay();
//		factoryControl.replay();
//		
//		interceptor.preHandle(request, response, "handler");
//		assertTrue(TransactionSynchronizationManager.hasResource(factory));
//		
//		// check that further invocations simply participate
//		interceptor.preHandle(request, response, "handler");
//
//		interceptor.preHandle(request, response, "handler");
//		interceptor.postHandle(request, response, "handler", null);
//		interceptor.afterCompletion(request, response, "handler", null);
//
//		interceptor.postHandle(request, response, "handler", null);
//		interceptor.afterCompletion(request, response, "handler", null);
//
//		interceptor.preHandle(request, response, "handler");
//		interceptor.postHandle(request, response, "handler", null);
//		interceptor.afterCompletion(request, response, "handler", null);
//		
//		factoryControl.verify();
//		managerControl.verify();
//		
//		managerControl.reset();
//		factoryControl.reset();
//		managerControl.replay();
//		factoryControl.replay();
//		
//		interceptor.postHandle(request, response, "handler", null);
//		assertTrue(TransactionSynchronizationManager.hasResource(factory));
//		
//		factoryControl.verify();
//		managerControl.verify();
//		
//		managerControl.reset();
//		factoryControl.reset();
//		
//		manager.close();
//		
//		managerControl.replay();
//		factoryControl.replay();
//		
//		interceptor.afterCompletion(request, response, "handler", null);
//		assertFalse(TransactionSynchronizationManager.hasResource(factory));
//		
//		factoryControl.verify();
//		managerControl.verify();
//	}
//	
//	public void testOpenEntityManagerInViewFilter() throws Exception {
//		manager.close();
//		
//		managerControl.replay();
//		factoryControl.replay();
//		
//		MockControl factoryControl2 = MockControl.createControl(EntityManagerFactory.class);
//		final EntityManagerFactory factory2 = (EntityManagerFactory) factoryControl2.getMock();
//		
//		MockControl managerControl2 = MockControl.createControl(EntityManager.class);
//		EntityManager manager2 = (EntityManager) managerControl2.getMock();
//		
//		factoryControl2.expectAndReturn(factory2.createEntityManager(), manager2);
//		manager2.close();
//		
//		factoryControl2.replay();
//		managerControl2.replay();
//
//		MockServletContext sc = new MockServletContext();
//		StaticWebApplicationContext wac = new StaticWebApplicationContext();
//		wac.setServletContext(sc);
//		wac.getDefaultListableBeanFactory().registerSingleton("entityManagerFactory", factory);
//		wac.getDefaultListableBeanFactory().registerSingleton("myEntityManagerFactory", factory2);
//		wac.refresh();
//		sc.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
//		MockHttpServletRequest request = new MockHttpServletRequest(sc);
//		MockHttpServletResponse response = new MockHttpServletResponse();
//
//		MockFilterConfig filterConfig = new MockFilterConfig(wac.getServletContext(), "filter");
//		MockFilterConfig filterConfig2 = new MockFilterConfig(wac.getServletContext(), "filter2");
//		filterConfig2.addInitParameter("entityManagerFactoryBeanName", "myEntityManagerFactory");
//
//		final OpenEntityManagerInViewFilter filter = new OpenEntityManagerInViewFilter();
//		filter.init(filterConfig);
//		final OpenEntityManagerInViewFilter filter2 = new OpenEntityManagerInViewFilter();
//		filter2.init(filterConfig2);
//
//		final FilterChain filterChain = new FilterChain() {
//			public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) {
//				assertTrue(TransactionSynchronizationManager.hasResource(factory));
//				servletRequest.setAttribute("invoked", Boolean.TRUE);
//			}
//		};
//
//		final FilterChain filterChain2 = new FilterChain() {
//			public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
//			    throws IOException, ServletException {
//				assertTrue(TransactionSynchronizationManager.hasResource(factory2));
//				filter.doFilter(servletRequest, servletResponse, filterChain);
//			}
//		};
//
//		FilterChain filterChain3 = new PassThroughFilterChain(filter2, filterChain2);
//
//		assertFalse(TransactionSynchronizationManager.hasResource(factory));
//		assertFalse(TransactionSynchronizationManager.hasResource(factory2));
//		filter2.doFilter(request, response, filterChain3);
//		assertFalse(TransactionSynchronizationManager.hasResource(factory));
//		assertFalse(TransactionSynchronizationManager.hasResource(factory2));
//		assertNotNull(request.getAttribute("invoked"));
//
//		factoryControl.verify();
//		managerControl.verify();
//		factoryControl2.verify();
//		managerControl2.verify();
//
//		wac.close();
//	}

}
