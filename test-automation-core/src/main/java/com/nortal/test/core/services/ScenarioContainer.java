package com.nortal.test.core.services;

import com.nortal.test.core.model.ScenarioContext;
import com.nortal.test.postman.PostmanScenarioRequestContextProvider;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Container for the cucumber scenario object.
 *
 * <p>Cucumber framework is very stingy on allowing to access the scenario object
 * and only lets us grab it in before and after hooks. That's why we need a container component that would hold the reference during the actual
 * scenario steps.
 *
 * <p>Users beware that its possible that the container will be empty if it is accessed before scenario starts.
 */
@Slf4j
@Component
public class ScenarioContainer implements PostmanScenarioRequestContextProvider {

	private final ThreadLocal<Scenario> scenario = new ThreadLocal<>();
	private final ThreadLocal<ScenarioContext> scenarioContext = new ThreadLocal<>();

	public void prepare(final Scenario scenario) {
		this.scenario.set(scenario);
		this.scenarioContext.set(new ScenarioContext());
	}

	public void clean() {
		this.scenario.remove();
		this.scenarioContext.remove();
	}

	public Scenario getScenario() {
		return scenario.get();
	}

	@Override
	public ScenarioContext get() {
		return scenarioContext.get();
	}

}