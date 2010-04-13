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

package org.springframework.context.refresh;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationListener;
import org.springframework.context.expression.BeanFactoryAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

/**
 * @author Dave Syer
 * 
 * @since 3.1
 * 
 */
public class RefreshScope implements Scope, BeanFactoryPostProcessor, ApplicationListener<RefreshScopeEvent> {

	private Map<String, Object> cache = new ConcurrentHashMap<String, Object>();

	private String name = "refresh";

	private Map<String, Runnable> callbacks = new ConcurrentHashMap<String, Runnable>();

	private boolean proxyTargetClass = false;

	private ConfigurableListableBeanFactory beanFactory;

	private StandardEvaluationContext evaluationContext;

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param proxyTargetClass the proxyTargetClass to set
	 */
	public void setProxyTargetClass(boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
	}

	public Object get(String name, ObjectFactory<?> objectFactory) {
		if (cache.containsKey(name)) {
			return cache.get(name);
		}
		Object value = objectFactory.getObject();
		cache.put(name, value);
		return value;
	}

	public String getConversationId() {
		return name;
	}

	public void registerDestructionCallback(String name, Runnable callback) {
		callbacks.put(name, callback);
	}

	public Object remove(String name) {
		return cache.remove(name);
	}

	public Object resolveContextualObject(String key) {
		Expression expression = parseExpression(key);
		return expression.getValue(evaluationContext, beanFactory);
	}

	private Expression parseExpression(String input) {
		if (StringUtils.hasText(input)) {
			ExpressionParser parser = new SpelExpressionParser();
			try {
				return parser.parseExpression(input);
			}
			catch (ParseException e) {
				throw new IllegalArgumentException("Cannot parse expression: " + input, e);
			}

		}
		else {
			return null;
		}
	}

	public void onApplicationEvent(RefreshScopeEvent event) {
		cache.clear();
		List<Throwable> errors = new ArrayList<Throwable>();
		for (Runnable callback : callbacks.values()) {
			try {
				callback.run();
			}
			catch (Throwable e) {
				errors.add(e);
			}
		}
		if (!errors.isEmpty()) {
			throw wrapIfNecessary(errors.get(0));
		}
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

		beanFactory.registerScope(name, this);

		this.beanFactory = beanFactory;
		evaluationContext = new StandardEvaluationContext();
		evaluationContext.addPropertyAccessor(new BeanFactoryAccessor());

		Assert.state(beanFactory instanceof BeanDefinitionRegistry,
				"BeanFactory was not a BeanDefinitionRegistry, so StepScope cannot be used.");
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

		for (String beanName : beanFactory.getBeanDefinitionNames()) {
			BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
			// Replace this or any of its inner beans with scoped proxy if it
			// has this scope
			boolean scoped = name.equals(definition.getScope());
			Scopifier scopifier = new Scopifier(registry, name, proxyTargetClass, scoped);
			scopifier.visitBeanDefinition(definition);
			if (scoped) {
				createScopedProxy(beanName, definition, registry, proxyTargetClass);
			}
		}

	}

	private static RuntimeException wrapIfNecessary(Throwable throwable) {
		if (throwable instanceof RuntimeException) {
			return (RuntimeException) throwable;
		}
		if (throwable instanceof Error) {
			throw (Error) throwable;
		}
		return new IllegalStateException(throwable);
	}

	private static BeanDefinitionHolder createScopedProxy(String beanName, BeanDefinition definition,
			BeanDefinitionRegistry registry, boolean proxyTargetClass) {
		BeanDefinitionHolder proxyHolder = ScopedProxyUtils.createScopedProxy(new BeanDefinitionHolder(definition,
				beanName), registry, proxyTargetClass);
		registry.registerBeanDefinition(beanName, proxyHolder.getBeanDefinition());
		return proxyHolder;
	}

	/**
	 * Helper class to scan a bean definition hierarchy and force the use of
	 * auto-proxy for step scoped beans.
	 * 
	 * @author Dave Syer
	 * 
	 */
	private static class Scopifier extends BeanDefinitionVisitor {

		private final boolean proxyTargetClass;

		private final BeanDefinitionRegistry registry;

		private final String scope;

		private final boolean scoped;

		public Scopifier(BeanDefinitionRegistry registry, String scope, boolean proxyTargetClass, boolean scoped) {
			super(new StringValueResolver() {
				public String resolveStringValue(String value) {
					return value;
				}
			});
			this.registry = registry;
			this.proxyTargetClass = proxyTargetClass;
			this.scope = scope;
			this.scoped = scoped;
		}

		@Override
		protected Object resolveValue(Object value) {

			BeanDefinition definition = null;
			String beanName = null;
			if (value instanceof BeanDefinition) {
				definition = (BeanDefinition) value;
				beanName = BeanDefinitionReaderUtils.generateBeanName(definition, registry);
			}
			else if (value instanceof BeanDefinitionHolder) {
				BeanDefinitionHolder holder = (BeanDefinitionHolder) value;
				definition = holder.getBeanDefinition();
				beanName = holder.getBeanName();
			}

			if (definition != null) {
				boolean nestedScoped = scope.equals(definition.getScope());
				boolean scopeChangeRequiresProxy = !scoped && nestedScoped;
				if (scopeChangeRequiresProxy) {
					// Exit here so that nested inner bean definitions are not
					// analysed
					return createScopedProxy(beanName, definition, registry, proxyTargetClass);
				}
			}

			// Nested inner bean definitions are recursively analysed here
			value = super.resolveValue(value);
			return value;

		}

	}

}
