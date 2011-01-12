package org.springframework.context.annotation;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Henryk Konsek
 * @author Chris Beams
 */
public class AnnotationTest {

	@Test
	public void annotation() {
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(Config.class);
		applicationContext.getBean(RegisteredBean.class);
	}

	@Test
	public void xml() {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("config.xml", this.getClass());
		applicationContext.getBean(RegisteredBean.class);
	}

	@Test
	public void xml2() {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("config2.xml", this.getClass());
		applicationContext.getBean(RegisteringBDRPPBean.class);
		applicationContext.getBean(RegisteredBDRPPBean.class);
		applicationContext.getBean(RegisteredBean.class);
	}

}

@Configuration
class Config {
	@Bean
	public RegisteringBean registeringBean() {
		return new RegisteringBean();
	}
}
