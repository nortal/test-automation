package com.nortal.test.core.plugin

import com.nortal.test.core.plugin.JsonPerFeatureReporter.readSources
import com.nortal.test.core.plugin.JsonPerFeatureReporter.addTestCase
import com.nortal.test.core.plugin.JsonPerFeatureReporter.startStep
import com.nortal.test.core.plugin.JsonPerFeatureReporter.finishStep
import com.nortal.test.core.plugin.JsonPerFeatureReporter.finishTestCase
import com.nortal.test.core.plugin.JsonPerFeatureReporter.addText
import com.nortal.test.core.plugin.JsonPerFeatureReporter.embed
import io.cucumber.plugin.EventListener
import io.cucumber.plugin.event.*

/**
 *
 * This class is a cucumber plugin that outputs tests results in a json format.
 * The main difference between this and standard json plugin is that this one will create a separate json file per feature.
 *
 *
 * Usage: plugin should be used as any other cucumber plugin by passing it to the runner.
 * <pre>`plugin = { "com.nortal.test.commons.plugin.JsonPerFeatureFormatterPlugin:buid/test-output/cucumber-report" }`</pre>
 *
 * Output folder should be provided by passing in a relative path as a parameter (the part after colon)
 */
class JsonPerFeatureFormatterPlugin(outputPath: String?) : EventListener {
    private val reporter: JsonPerFeatureReporter

    init {
        reporter = JsonPerFeatureReporter(outputPath)
    }

    override fun setEventPublisher(publisher: EventPublisher) {
        publisher.registerHandlerFor(TestSourceRead::class.java) { event: TestSourceRead? -> reporter.readSources(event) }
        publisher.registerHandlerFor(TestCaseStarted::class.java) { event: TestCaseStarted? ->
            reporter.addTestCase(
                event!!
            )
        }
        publisher.registerHandlerFor(TestStepStarted::class.java) { event: TestStepStarted? ->
            reporter.startStep(
                event!!
            )
        }
        publisher.registerHandlerFor(TestStepFinished::class.java) { event: TestStepFinished? ->
            reporter.finishStep(
                event!!
            )
        }
        publisher.registerHandlerFor(TestCaseFinished::class.java) { event: TestCaseFinished? ->
            reporter.finishTestCase(
                event!!
            )
        }
        publisher.registerHandlerFor(WriteEvent::class.java) { event: WriteEvent? ->
            reporter.addText(
                event!!
            )
        }
        publisher.registerHandlerFor(EmbedEvent::class.java) { event: EmbedEvent? ->
            reporter.embed(
                event!!
            )
        }
    }
}