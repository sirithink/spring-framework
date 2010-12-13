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

import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AbstractPropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ChainedPropertyResolver;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.util.Assert;
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
 * @see EnvironmentAwarePropertyOverrideConfigurer
 */
public class EnvironmentAwarePropertyPlaceholderConfigurer
		extends AbstractPropertyPlaceholderConfigurer implements EnvironmentAware {

	private ChainedPropertyResolver propertyResolver;
	private Environment environment;

	public void setEnvironment(Environment environment) {
		this.environment = environment;
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
		Assert.notNull(this.environment, "Environment must not be null. Did you call setEnvironment()?");

		PropertySources localPropertySources = new PropertySources();
		if (this.localProperties != null) {
			int cx=0;
			for (Properties localProps : this.localProperties) {
				localPropertySources.addLast(new PropertiesPropertySource("localProperties"+cx++, localProps));
			}
		}

		PropertyResolver localPropertyResolver = new PropertySourcesPropertyResolver(localPropertySources);
		PropertyResolver environmentPropertyResolver = this.environment.getPropertyResolver();
		propertyResolver = new ChainedPropertyResolver();

		if (this.localOverride) {
			propertyResolver.addResolver(localPropertyResolver);
		} 

		this.propertyResolver.addResolver(environmentPropertyResolver);

		if (!this.localOverride) {
			propertyResolver.addResolver(localPropertyResolver);
		}

		super.postProcessBeanFactory(beanFactory);
	}

}
