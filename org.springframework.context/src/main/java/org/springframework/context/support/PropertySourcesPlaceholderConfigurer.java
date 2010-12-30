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

package org.springframework.context.support;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.AbstractPropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;


/**
 * TODO SPR-7508: document
 *
 * Local properties are added as a property source in any case. Precedence is based
 * on the value of the {@link #setLocalOverride(boolean) localOverride} property.
 *
 * @author Chris Beams
 * @since 3.1
 * @see PropertyPlaceholderConfigurer
 */
public class PropertySourcesPlaceholderConfigurer
		extends AbstractPropertyPlaceholderConfigurer implements EnvironmentAware {

	private MutablePropertySources propertySources;
	private PropertyResolver propertyResolver;
	private Environment environment;


	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	/**
	 * TODO SPR-7508: document
	 */
	public void setPropertySources(PropertySources propertySources) {
		this.propertySources = new MutablePropertySources(propertySources);
	}

	@Override
	protected PlaceholderResolver getPlaceholderResolver(Properties props) {
		return new PlaceholderResolver() {
			public String resolvePlaceholder(String placeholderName) {
				return propertyResolver.getProperty(placeholderName);
			}
		};
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (this.propertySources == null) {
			this.propertySources = new MutablePropertySources();
			if (this.environment != null) {
				this.propertySources.addAll(this.environment.getPropertySources());
			}
			try {
				PropertiesPropertySource localPropertySource = new PropertiesPropertySource("localProperties", mergeProperties());
				if (this.localOverride) {
					this.propertySources.addFirst(localPropertySource);
				} else {
					this.propertySources.addLast(localPropertySource);
				}
			}
			catch (IOException ex) {
				throw new BeanInitializationException("Could not load properties", ex);
			}
		}

		this.propertyResolver = new PropertySourcesPropertyResolver(this.propertySources);
		this.processProperties(beanFactory, this.propertyResolver.asProperties());
	}

}
