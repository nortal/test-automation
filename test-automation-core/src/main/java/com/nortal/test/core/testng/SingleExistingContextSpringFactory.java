package com.nortal.test.core.testng;


import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.exception.CucumberException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.Collection;
import java.util.HashSet;

/**
 * This shares a single spring application context with all threads in the test execution jvm. Default cucumber currently (7/3/2019) creates separate
 * context for each thread and its not working well.
 *
 * @author VJose6
 */
public class SingleExistingContextSpringFactory implements ObjectFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(SingleExistingContextSpringFactory.class);

	static SpringContextHolder CONTEXT_HOLDER;

	private final Collection<Class<?>> stepClasses = new HashSet<>();

	@Override
	public boolean addClass(final Class<?> stepClass) {
		if (!stepClasses.contains(stepClass)) {
			stepClasses.add(stepClass);
		}
		return true;
	}

	@Override
	public void start() {
		notifyContextManagerAboutTestClassStarted();
		ConfigurableListableBeanFactory beanFactory = CONTEXT_HOLDER.getApplicationContext().getBeanFactory();
		for (Class<?> stepClass : stepClasses) {
			registerStepClassBeanDefinition(beanFactory, stepClass);
		}
	}

	private void notifyContextManagerAboutTestClassStarted() {
		try {
			CONTEXT_HOLDER.beforeTestClass();
		} catch (Exception e) {
			throw new CucumberException(e.getMessage(), e);
		}
	}

	private void registerStepClassBeanDefinition(ConfigurableListableBeanFactory beanFactory, Class<?> stepClass) {
		synchronized (CONTEXT_HOLDER) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			BeanDefinition beanDefinition = BeanDefinitionBuilder
					.genericBeanDefinition(stepClass)
					.setScope(BeanDefinition.SCOPE_SINGLETON)
					.getBeanDefinition();

			if (registry.containsBeanDefinition(stepClass.getName())) {
				LOGGER.debug("Step class {} was already registered. This is a workaround for shared spring context. Ignoring..", stepClass.getName());
			} else {
				registry.registerBeanDefinition(stepClass.getName(), beanDefinition);
			}
		}
	}

	@Override
	public <T> T getInstance(final Class<T> type) {
		synchronized (CONTEXT_HOLDER) {
			try {
				return CONTEXT_HOLDER.getApplicationContext().getBean(type);
			} catch (BeansException e) {
				throw new CucumberException(e.getMessage(), e);
			}
		}
	}

	@Override
	public void stop() {
		notifyContextManagerAboutTestClassFinished();
	}

	private void notifyContextManagerAboutTestClassFinished() {
		try {
			CONTEXT_HOLDER.afterTestClass();
		} catch (Exception e) {
			throw new CucumberException(e.getMessage(), e);
		}
	}

}
