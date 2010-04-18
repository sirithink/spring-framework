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

package org.springframework.beans.factory.support;

import java.util.Properties;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.ClassUtils;
import org.springframework.util.CompositePropertyResolver;
import org.springframework.util.PropertyResolver;
import org.springframework.util.PropertyResolverFactory;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertiesPropertyResolver;

/**
 * @author Dave Syer
 * @since 3.1
 * 
 */
public class SimplePropertyResolverFactory implements PropertyResolverFactory {

	public static final String RESOLVER_CLASS_NAMES = SimplePropertyResolverFactory.class.getName() + ".classes";

	public PropertyResolver getResolver(ClassLoader classLoader, Properties properties) {

		String classNames = properties
				.getProperty(RESOLVER_CLASS_NAMES, SystemPropertiesPropertyResolver.class.getName());

		CompositePropertyResolver result = new CompositePropertyResolver();

		for (String className : StringUtils.commaDelimitedListToStringArray(classNames)) {
			result.addPropertyResolver(instantiateClass(classLoader, className));
		}

		return result;

	}

	/**
	 * @param classLoader
	 * @param className the type of the object to create
	 * @return an instance from the default constructor
	 */
	private PropertyResolver instantiateClass(ClassLoader classLoader, String className) {

		try {

			@SuppressWarnings("unchecked")
			Class<PropertyResolver> handlerClass = (Class<PropertyResolver>) ClassUtils.forName(className, classLoader);

			if (!PropertyResolver.class.isAssignableFrom(handlerClass)) {
				throw new FatalBeanException("Class [" + className + "] does not implement the ["
						+ PropertyResolver.class.getName() + "] interface");
			}

			return BeanUtils.instantiateClass(handlerClass);

		}
		catch (ClassNotFoundException ex) {

			throw new FatalBeanException("StringValueResolver class [" + className + "] not found", ex);

		}
		catch (LinkageError err) {

			throw new FatalBeanException("Invalid StringValueResolver class [" + className
					+ "]: problem with resolver class file or dependent class", err);

		}

	}

}
