package org.springframework.context.annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author Henryk Konsek
 */
public class RegisteringBean implements BeanDefinitionRegistryPostProcessor {

	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		System.out
				.println("RegisteringBean.postProcessBeanDefinitionRegistry()");
		RootBeanDefinition beanDefinition = new RootBeanDefinition(RegisteredBean.class);
		registry.registerBeanDefinition(RegisteredBean.class.getSimpleName(), beanDefinition);
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("RegisteringBean.postProcessBeanFactory()");
	}

}
