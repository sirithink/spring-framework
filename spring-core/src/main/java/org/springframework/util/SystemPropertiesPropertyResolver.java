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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Dave Syer
 *
 */
public class SystemPropertiesPropertyResolver implements PropertyResolver {

	private static final Log logger = LogFactory.getLog(SystemPropertiesPropertyResolver.class);

	private final String text;

	public SystemPropertiesPropertyResolver(String text) {
		this.text = text;
	}

	public SystemPropertiesPropertyResolver() {
		this(null);
	}


	public String resolveStringValue(String variable) {
		try {
			String propVal = System.getProperty(variable);
			if (propVal == null) {
				// Fall back to searching the system environment.
				propVal = System.getenv(variable);
			}
			return propVal;
		} catch (Throwable ex) {
			if (logger.isInfoEnabled()) {
				logger.info("Could not resolve placeholder '" + variable + "' in ["
						+ (this.text == null ? variable : this.text) + "] as system property: " + ex);
			}
			return null;
		}
	}

}