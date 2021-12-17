package com.nortal.test.core.plugin

import io.cucumber.plugin.ConcurrentEventListener
import io.cucumber.plugin.event.EventPublisher
import io.cucumber.plugin.event.TestCaseFinished
import io.cucumber.plugin.event.TestCaseStarted
import io.cucumber.plugin.event.TestStepStarted
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class FlowLoggingPlugin : ConcurrentEventListener {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)
    override fun setEventPublisher(publisher: EventPublisher) {
        publisher.registerHandlerFor(TestCaseStarted::class.java) { event: TestCaseStarted ->
            log.info(
                "Executing scenario: {}",
                event.testCase.name
            )
        }
        publisher.registerHandlerFor(TestStepStarted::class.java) { event: TestStepStarted ->
            log.info(
                "Step: {}",
                event.testCase.name
            )
        }
        publisher.registerHandlerFor(TestCaseFinished::class.java) { event: TestCaseFinished ->
            log.info(
                "Executing scenario: {}",
                event.testCase.name
            )
        }
    }
}