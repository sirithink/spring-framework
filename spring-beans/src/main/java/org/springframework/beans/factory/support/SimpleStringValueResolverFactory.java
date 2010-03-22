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

package org.springframework.beans.factory.support;

import java.util.Properties;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.ClassUtils;
import org.springframework.util.CompositeStringValueResolver;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;
import org.springframework.util.StringValueResolverFactory;
import org.springframework.util.SystemPropertyStringValueResolver;

/**
 * @author Dave Syer
 * @since 3.1
 * 
 */
public class SimpleStringValueResolverFactory implements
		StringValueResolverFactory {

	public static final String RESOLVER_CLASS_NAMES = SimpleStringValueResolverFactory.class
			.getName()
			+ ".classes";

	public StringValueResolver getResolver(ClassLoader classLoader,
			Properties properties) {

		String classNames = properties.getProperty(RESOLVER_CLASS_NAMES,
				SystemPropertyStringValueResolver.class.getName());

		CompositeStringValueResolver result = new CompositeStringValueResolver();

		for (String className : StringUtils
				.commaDelimitedListToStringArray(classNames)) {
			result.addStringValueResolver(instantiateClass(classLoader,
					className));
		}

		return result;

	}

	/**
	 * @param classLoader
	 * @param className
	 *            the type of the object to create
	 * @return an instance from the default constructor
	 */
	private StringValueResolver instantiateClass(ClassLoader classLoader,
			String className) {

		try {

			@SuppressWarnings("unchecked")
			Class<StringValueResolver> handlerClass = (Class<StringValueResolver>) ClassUtils
					.forName(className, classLoader);

			if (!StringValueResolver.class.isAssignableFrom(handlerClass)) {
				throw new FatalBeanException("Class [" + className
						+ "] does not implement the ["
						+ StringValueResolver.class.getName() + "] interface");
			}

			return BeanUtils.instantiateClass(handlerClass);

		} catch (ClassNotFoundException ex) {

			throw new FatalBeanException("StringValueResolver class ["
					+ className + "] not found", ex);

		} catch (LinkageError err) {

			throw new FatalBeanException("Invalid StringValueResolver class ["
					+ className
					+ "]: problem with resolver class file or dependent class",
					err);

		}

	}

}
