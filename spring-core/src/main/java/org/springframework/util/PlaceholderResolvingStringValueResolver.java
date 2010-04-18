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

package org.springframework.util;

import org.springframework.core.Ordered;

/**
 * Adapts a {@link PropertyResolver} to a {@link StringValueResolver} that replaces placeholders in its input.
 * 
 * @author Dave Syer
 * 
 * @since 3.1
 * 
 */
public class PlaceholderResolvingStringValueResolver implements StringValueResolver, Ordered {

	private final PropertyPlaceholderHelper helper;

	private final PropertyResolver resolver;

	private int order = Ordered.LOWEST_PRECEDENCE;

	public PlaceholderResolvingStringValueResolver(PropertyPlaceholderHelper helper, PropertyResolver resolver) {
		this.helper = helper;
		this.resolver = resolver;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	public String resolveStringValue(String strVal) {
		return this.helper.replacePlaceholders(strVal, this.resolver);
	}
}