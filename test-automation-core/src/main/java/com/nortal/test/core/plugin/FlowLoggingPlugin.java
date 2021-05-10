package com.nortal.test.core.plugin;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStepStarted;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlowLoggingPlugin implements ConcurrentEventListener {

	@Override
	public void setEventPublisher(EventPublisher publisher) {

		publisher.registerHandlerFor(TestCaseStarted.class, event ->  log.info("Executing scenario: {}",event.getTestCase().getName()));

		publisher.registerHandlerFor(TestStepStarted.class, event -> log.info("Step: {}",event.getTestCase().getName()));

		publisher.registerHandlerFor(TestCaseFinished.class, event -> log.info("Executing scenario: {}",event.getTestCase().getName()));

	}

}
