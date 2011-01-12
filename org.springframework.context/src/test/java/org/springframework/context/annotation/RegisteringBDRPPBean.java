package org.springframework.context.annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author Chris Beams
 */
public class RegisteringBDRPPBean implements BeanDefinitionRegistryPostProcessor {

	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		System.out.println("RegisteringBDRPPBean.postProcessBeanDefinitionRegistry()");
		RootBeanDefinition beanDefinition = new RootBeanDefinition(RegisteredBDRPPBean.class);
		registry.registerBeanDefinition(RegisteredBDRPPBean.class.getSimpleName(), beanDefinition);
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("RegisteringBDRPPBean.postProcessBeanFactory()");
	}

}

class RegisteredBDRPPBean implements BeanDefinitionRegistryPostProcessor {

	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		System.out.println("RegisteredBDRPPBean.postProcessBeanDefinitionRegistry()");
		RootBeanDefinition beanDefinition = new RootBeanDefinition(RegisteredBean.class);
		registry.registerBeanDefinition(RegisteredBean.class.getSimpleName(), beanDefinition);
	}

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("RegisteredBDRPPBean.postProcessBeanFactory()");
	}

}