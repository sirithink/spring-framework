/*
 * Copyright 2009-2010 the original author or authors.
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
package org.springframework.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;

/**
 * Helper class for composite patterns involving {@link Ordered}. Accepts and organizes a list of items and sorts them
 * into order based on the Ordered interface and annotation. Any unordered items come after the ordered ones (in the
 * order they are added to the list).
 * 
 * @author Dave Syer
 * 
 * @since 3.1
 * 
 */
public class OrderedCompositeHelper<S> {

	private List<S> unordered = new ArrayList<S>();

	private Collection<S> ordered = new TreeSet<S>(new AnnotationAwareOrderComparator());

	private List<S> list = new ArrayList<S>();

	/**
	 * Convenient default constructor.  Start with an empty list of items.
	 */
	public OrderedCompositeHelper() {
	}

	/**
	 * Initialize a helper with the items provided.
	 * 
	 * @param items the items to add to the list
	 */
	public OrderedCompositeHelper(List<S> items) {
		super();
		setItems(items);
	}

	/**
	 * Public setter for the items.
	 * 
	 * @param items
	 */
	public void setItems(List<? extends S> items) {
		unordered.clear();
		ordered.clear();
		for (S s : items) {
			add(s);
		}
	}

	/**
	 * Register additional item.
	 * 
	 * @param item
	 */
	public void add(S item) {
		if (item instanceof Ordered) {
			if (!ordered.contains(item)) {
				ordered.add(item);
			}
		} else if (AnnotationUtils.isAnnotationDeclaredLocally(Order.class, item.getClass())) {
			if (!ordered.contains(item)) {
				ordered.add(item);
			}
		} else if (!unordered.contains(item)) {
			unordered.add(item);
		}
		list.clear();
		list.addAll(ordered);
		list.addAll(unordered);
	}

	/**
	 * Public getter for the list of items. The {@link Ordered} items come
	 * first, followed by any unordered ones.
	 * @return an ordered list of items (mutable but not backed by the source list)
	 */
	public List<S> forward() {
		return new ArrayList<S>(list);
	}

	/**
	 * Public getter for the list of items in reverse. The {@link Ordered} items
	 * come last, after any unordered ones.
	 * @return an ordered list of items (mutable but not backed by the source list)
	 */
	public List<S> reverse() {
		ArrayList<S> result = new ArrayList<S>(list);
		Collections.reverse(result);
		return result;
	}

	public int size() {
		return list.size();
	}

}
