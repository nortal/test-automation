package com.nortal.test.core.testng;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * Abstracts the context holder from this package
 * 
 * @author VJose6
 *
 */
public interface SpringContextHolder {
	
	ConfigurableApplicationContext getApplicationContext();

	void beforeTestClass() throws Exception;

	void afterTestClass() throws Exception;

}
