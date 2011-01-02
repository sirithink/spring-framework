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

package org.springframework.web.portlet.context;


import org.springframework.core.env.DefaultEnvironment;
import org.springframework.core.env.PropertySource.StubPropertySource;
import org.springframework.web.context.support.DefaultWebEnvironment;


/**
 * TODO SPR-7508: document
 *
 * @author Chris Beams
 * @since 3.1
 * @see PortletApplicationContextUtils#initServletPropertySources
 */
public class DefaultPortletEnvironment extends DefaultEnvironment {

	public static final String PORTLET_CONTEXT_PROPERTY_SOURCE_NAME = "portletContextInitParams";
	public static final String PORTLET_CONFIG_PROPERTY_SOURCE_NAME = "portletConfigInitParams";

	public DefaultPortletEnvironment() {
		this.getPropertySources().addFirst(new StubPropertySource(DefaultWebEnvironment.SERVLET_CONTEXT_PROPERTY_SOURCE_NAME));
		this.getPropertySources().addFirst(new StubPropertySource(PORTLET_CONTEXT_PROPERTY_SOURCE_NAME));
		this.getPropertySources().addFirst(new StubPropertySource(PORTLET_CONFIG_PROPERTY_SOURCE_NAME));
	}
}
