/*
 * Copyright 2002-2011 the original author or authors.
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

import java.util.Map;

/**
 * {@link PropertySource} that reads keys and values from a {@code Map<String,String>} object.
 *
 * @author Chris Beams
 * @since 3.1
 * @see PropertiesPropertySource
 */
public class MapPropertySource extends PropertySource<Map<String, String>> {

	protected MapPropertySource(String name, Map<String, String> source) {
		super(name, source);
	}

	@Override
	public boolean containsProperty(String key) {
		return source.containsKey(key);
	}

	@Override
	public String getProperty(String key) {
		return source.get(key);
	}

	@Override
	public int size() {
		return source.size();
	}

}