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
package org.springframework.util;

import java.util.Properties;

/**
 * @author Dave Syer
 * 
 */
public class PropertiesStringValueResolver implements StringValueResolver {

	private Properties properties;
	
	public PropertiesStringValueResolver() {
	}

	/**
	 * @param properties
	 */
	public PropertiesStringValueResolver(Properties properties) {
		super();
		this.properties = properties;
	}
	
	/**
	 * @param properties the Properties to set
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	public String resolveStringValue(String strVal) {
		return properties.getProperty(strVal);
	}

}
