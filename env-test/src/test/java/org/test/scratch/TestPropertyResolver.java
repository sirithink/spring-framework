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

package org.test.scratch;

import java.util.Properties;

import org.springframework.util.PropertyResolver;

public class TestPropertyResolver implements PropertyResolver {
	
	private static Properties properties = new Properties();
	
	static public void setProperty(String key, String value) {
		properties.setProperty(key, value);
	}
	
	static public void remove(String key) {
		properties.remove(key);
	}
		
	public String resolveStringValue(String strVal) {
		if (properties.containsKey(strVal)) {
			return properties.getProperty(strVal);
		}
		if (strVal.equals("jdbc.type")) {
			return "ORACLE";
		}
		if (strVal.equals("app")) {
			return "app";
		}
		if (strVal.equals("scratch")) {
			return "scratch";
		}
		if (strVal.equals("context")) {
			return "context";
		}
		return null;
	}
}