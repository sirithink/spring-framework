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

package org.springframework.util;

import java.util.List;

import org.springframework.core.OrderedCompositeHelper;

/**
 * @author Dave Syer
 *
 */
public class CompositePropertyResolver implements PropertyResolver {

	private OrderedCompositeHelper<PropertyResolver> propertyResolvers = new OrderedCompositeHelper<PropertyResolver>();

	/**
	 * @param propertyResolvers the string value resolvers to set
	 */
	public void setStringValueResolvers(List<? extends PropertyResolver> propertyResolvers) {
		this.propertyResolvers.setItems(propertyResolvers);
	}
	
	/**
	 * @param propertyResolver the string value resolver to add
	 */
	public void addPropertyResolver(PropertyResolver propertyResolver) {
		this.propertyResolvers.add(propertyResolver);
	}	

	public String resolveStringValue(String variable) {
		for (PropertyResolver resolver : propertyResolvers.forward()) {
			String resolved = resolver.resolveStringValue(variable);
			if (resolved != null) {
				return resolved;
			}
		}
		return null;
	}

	public int size() {
		return propertyResolvers.size();
	}
}