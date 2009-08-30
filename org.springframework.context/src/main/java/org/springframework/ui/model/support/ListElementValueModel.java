/*
 * Copyright 2004-2009 the original author or authors.
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
package org.springframework.ui.model.support;

import java.lang.reflect.Array;
import java.util.List;

import org.springframework.core.convert.TypeDescriptor;

/**
 * A ValueModel for a element in a List.
 * @author Keith Donald
 * @since 3.0
 */
public class ListElementValueModel implements ValueModel {

	private Object list;

	private int index;

	private Class<?> elementType;

	public ListElementValueModel(int index, Class<?> elementType, Object list) {
		this.index = index;
		this.elementType = elementType;
		this.list = list;
	}

	@SuppressWarnings("unchecked")
	public Object getValue() {
		if (list instanceof List) {
			return ((List)list).get(index);
		} else if (list.getClass().isArray()) {
			return Array.get(list, index);
		} else {
			return null;
		}
	}

	public Class<?> getValueType() {
		if (elementType != null) {
			return elementType;
		} else {
			return getValue().getClass();
		}
	}

	public TypeDescriptor getValueTypeDescriptor() {
		return TypeDescriptor.valueOf(getValueType());
	}

	public boolean isWriteable() {
		return true;
	}

	@SuppressWarnings("unchecked")
	public void setValue(Object value) {
		if (list instanceof List) {
			((List) list).set(index, value);
		} else if (list.getClass().isArray()) {
			Array.set(list, index, value);
		}
	}
}