package com.nortal.test.core.plugin;

import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestSourceRead;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import io.cucumber.plugin.event.WriteEvent;

/**
 *
 * This class is a cucumber plugin that outputs tests results in a json format.
 * The main difference between this and standard json plugin is that this one will create a separate json file per feature.
 * <p>
 * Usage: plugin should be used as any other cucumber plugin by passing it to the runner.
 * <pre><code>plugin = { "com.nortal.test.commons.plugin.JsonPerFeatureFormatterPlugin:test-output/cucumber-report" }</code></pre>
 * </p>
 * Output folder should be provided by passing in a relative path as a parameter (the part after colon)
 */
public class JsonPerFeatureFormatterPlugin implements EventListener {

    private JsonPerFeatureReporter reporter;

    public JsonPerFeatureFormatterPlugin(String outputPath) {
        this.reporter = new JsonPerFeatureReporter(outputPath);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, event -> reporter.readSources(event));
        publisher.registerHandlerFor(TestCaseStarted.class, event -> reporter.addTestCase(event));
        publisher.registerHandlerFor(TestStepStarted.class, event -> reporter.startStep(event));
        publisher.registerHandlerFor(TestStepFinished.class, event -> reporter.finishStep(event));
        publisher.registerHandlerFor(TestCaseFinished.class, event -> reporter.finishTestCase(event));
        publisher.registerHandlerFor(WriteEvent.class, event -> reporter.addText(event));
        publisher.registerHandlerFor(EmbedEvent.class, event -> reporter.embed(event));
    }

}
