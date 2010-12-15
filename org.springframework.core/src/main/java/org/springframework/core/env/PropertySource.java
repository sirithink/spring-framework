/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.core.env;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;


public abstract class PropertySource<T> {

	protected final Log logger = LogFactory.getLog(this.getClass());

	protected final String name;
	protected final T source;

	public PropertySource(String name, T source) {
		Assert.hasText(name, "Property source name must contain at least one character");
		Assert.notNull(source, "Property source must not be null");
		this.name = name;
		this.source = source;
	}

	public String getName() {
		return this.name;
	}

	public T getSource() {
		return source;
	}

	public abstract boolean containsProperty(String key);

	public abstract String getProperty(String key);

	public abstract int size();


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PropertySource))
			return false;
		PropertySource<?> other = (PropertySource<?>) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	/**
	 * Produce concise output (type, name, and number of properties) if the current log level does
	 * not include debug. If debug is enabled, produce verbose output including hashcode of the
	 * PropertySource instance and every key/value property pair.
	 *
	 * This variable verbosity is useful as a property source such as system properties
	 * or environment variables may contain an arbitrary number of property pairs, potentially
	 * leading to difficult to read exception and log messages.
	 *
	 * @see Log#isDebugEnabled()
	 */
	@Override
	public String toString() {
		if (logger.isDebugEnabled()) {
			return String.format("%s@%s [name='%s', properties=%s]",
					this.getClass().getSimpleName(), System.identityHashCode(this), this.name, this.source);
		}

		return String.format("%s [name='%s', propertyCount=%d]",
				this.getClass().getSimpleName(), this.name, this.size());
	}


	/**
	 * For collection comparison purposes
	 * TODO SPR-7508: document
	 */
	public static PropertySource<?> named(String name) {
		return new ComparisonPropertySource(name);
	}


	/**
	 * PropertySource to be used as a placeholder in cases where an actual
	 * property source cannot be eagerly initialized at application context
	 * creation time.  For example, a ServletCcontext-based property source
	 * must wait until the ServletContext object is available to its enclosing
	 * ApplicationContext.  In such cases, a stub should be used to hold the
	 * intended default position/order of the property source, then be replaced
	 * during context refresh.
	 *
	 * @see org.springframework.context.support.AbstractApplicationContext#initPropertySources()
	 * @see org.springframework.web.context.support.DefaultWebEnvironment
	 * @see org.springframework.web.context.support.ServletContextPropertySource
	 */
	public static class StubPropertySource extends PropertySource<Object> {

		public StubPropertySource(String name) {
			super(name, new Object());
		}

		@Override
		public boolean containsProperty(String key) {
			return false;
		}

		@Override
		public String getProperty(String key) {
			return null;
		}

		@Override
		public int size() {
			return 0;
		}
	}

	/**
	 * TODO: SPR-7508: document
	 */
	static class ComparisonPropertySource extends PropertySource<Object> {

		private static final String USAGE_ERROR =
			"ComparisonPropertySource instances are for collection comparison " +
			"use only";

		public ComparisonPropertySource(String name) {
			super(name, new Object());
		}

		@Override
		public Object getSource() {
			throw new UnsupportedOperationException(USAGE_ERROR);
		}
		public String getProperty(String key) {
			throw new UnsupportedOperationException(USAGE_ERROR);
		}
		public boolean containsProperty(String key) {
			throw new UnsupportedOperationException(USAGE_ERROR);
		}
		public int size() {
			throw new UnsupportedOperationException(USAGE_ERROR);
		}

		@Override
		public String toString() {
			return String.format("%s [name='%s']", getClass().getSimpleName(), this.name);
		}
	}
}
