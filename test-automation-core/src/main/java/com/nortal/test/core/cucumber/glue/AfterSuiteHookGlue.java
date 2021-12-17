package com.nortal.test.core.cucumber.glue;

import static java.util.Comparator.comparingInt;

import com.nortal.test.core.services.hooks.AfterSuiteHook;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Triggering {@link  AfterSuiteHook} beans before spring context is closed.
 * Note: we can't do the same for {@link  com.nortal.test.core.services.hooks.BeforeSuiteHook} due to the way cucumber initializes it.
 */
public class AfterSuiteHookGlue {
	private static final Logger log = LoggerFactory.getLogger(AfterSuiteHookGlue.class);

	private static ApplicationContext context;

	@Autowired
	public AfterSuiteHookGlue(final ApplicationContext appContext) {
		context = appContext;
	}

	@Before
	public void before() {
		//dummy, just to trigger initialization.
	}

	@AfterAll
	public static void afterSuite() {
		context.getBeansOfType(AfterSuiteHook.class).values().stream()
				.sorted(comparingInt(AfterSuiteHook::afterSuitOrder))
				.peek(it -> log.info("Running after suite hook for: {}", it.getClass().getName()))
				.forEach(AfterSuiteHook::afterSuite);
	}

}
