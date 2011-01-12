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

package org.springframework.context.annotation;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

public class Annotation2Tests {
	@Test
	public void repro() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		SomeOtherBDRPP someBDRPP = new SomeOtherBDRPP();
		ctx.addBeanFactoryPostProcessor(someBDRPP);
		ctx.register(MyConfiguration.class);
		ctx.refresh();
		MyBDRPP myBDRPP = ctx.getBean(MyBDRPP.class);
		assertTrue("MyBDRPP.ppBF was not called", myBDRPP.ppBFCalled);
		assertTrue("MyBDRPP.ppBDR was not called", myBDRPP.ppBDRCalled);

		assertTrue("SomeOtherBDRPP.ppBF was not called", someBDRPP.ppBFCalled);
		assertTrue("SomeOtherBDRPP.ppBDR was not called", someBDRPP.ppBDRCalled);
	}
}


@Configuration
class MyConfiguration {
	@Bean
	public MyBDRPP myBDRPP() {
		return new MyBDRPP();
	}
}

class MyBDRPP implements BeanDefinitionRegistryPostProcessor {

	boolean ppBFCalled;
	boolean ppBDRCalled;

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		this.ppBFCalled = true;
	}

	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		this.ppBDRCalled = true;
	}
}

class SomeOtherBDRPP implements BeanDefinitionRegistryPostProcessor {

	boolean ppBFCalled;
	boolean ppBDRCalled;

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("SomeOtherBDRPP.postProcessBeanFactory()");
		this.ppBFCalled = true;
	}

	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		System.out.println("SomeOtherBDRPP.postProcessBeanDefinitionRegistry()");
		this.ppBDRCalled = true;
	}
}