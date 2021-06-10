package com.nortal.test.core.glue;

import java.util.List;
import com.nortal.test.core.services.ScenarioContainer;
import com.nortal.test.core.services.hooks.AfterScenarioHook;
import com.nortal.test.core.services.hooks.BeforeScenarioHook;
import com.nortal.test.core.services.hooks.BeforeSuiteHook;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static java.util.Comparator.comparingInt;

@Slf4j
@RequiredArgsConstructor
public class Hooks {

	private static final Object LOCK = new Object();
	private static boolean dunit;
	private final List<AfterScenarioHook> afterScenarioHooks;
	private final List<BeforeScenarioHook> beforeScenarioHooks;
	private final List<BeforeSuiteHook> suitePreparations;
	private final ScenarioContainer container;

	/**
	 * Cucumber does not expose a way to retrieve Scenario variable in the middle of a step, thus we must resort to grabbin it at the start and
	 * keeping it.
	 */
	@Before(order = -1)
	public void prepareScenarioContext(final Scenario scenario) {
		log.info("Preparing scenario container");
		container.prepare(scenario);
	}

	/**
	 * This hooks sets up our environment. Since cucumber does not support BeforeSuite hooks we use a boolean to run it once only.
	 */
	@Before(order = 0)
	public void configurationHook() {
		synchronized (LOCK) {
			if (dunit) {
				return;
			}
			dunit = true;
			suitePreparations.stream()
							 .sorted(comparingInt(BeforeSuiteHook::beforeSuiteOrder))
							 .peek(it -> log.info("Running before suite hook for: {}", it.getClass().getName()))
							 .forEach(BeforeSuiteHook::beforeSuite);
		}
	}

	@Before(order = 1)
	public void beforeScenario() {
		beforeScenarioHooks.forEach(it -> it.before(container));
	}

	@After(order = Integer.MIN_VALUE)
	public void afterScenario() {
		afterScenarioHooks.forEach(hook -> hook.after(container));

		log.info("Cleaning scenario container");
		container.clean();
	}

}
