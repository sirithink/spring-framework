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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class ChainedPropertyResolver extends AbstractPropertyResolver {

	private List<PropertyResolver> resolvers = new ArrayList<PropertyResolver>();

	public void addResolver(PropertyResolver propertyResolver) {
		resolvers.add(propertyResolver);
	}

	public boolean containsProperty(String key) {
		for (PropertyResolver resolver : this.resolvers) {
			if (resolver.containsProperty(key)) {
				return true;
			}
		}
		return false;
	}

	public String getProperty(String key) {
		for (PropertyResolver resolver : this.resolvers) {
			String value = resolver.getProperty(key);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	public <T> T getProperty(String key, Class<T> targetType) {
		for (PropertyResolver resolver : this.resolvers) {
			T value = resolver.getProperty(key, targetType);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	public Properties asProperties() {
		Properties properties = new Properties();
		for (PropertyResolver resolver : this.resolvers) {
			properties.putAll(resolver.asProperties());
		}
		return properties;
	}

}
