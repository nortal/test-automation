package com.nortal.test.core.plugin;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.HookType;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.TestCaseEvent;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestSourceRead;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import io.cucumber.plugin.event.WriteEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is responsible for holding all the features from a test suite run.
 * It receives events and routes them to correct features.
 * It also responsible for persisting features to disk when needed.
 */
@Slf4j
public class JsonPerFeatureReporter {
    private static final Gson gson = new Gson();
    private final Path outputDir;

    private final Map<String, ReportFeature> reportFeaturesByFileName = new HashMap<>();
    private final FeatureSourcesHelper sourcesHelper = new FeatureSourcesHelper();

    public JsonPerFeatureReporter(String outputDir) {
        Path outputPath = Paths.get(outputDir);

        try {
            Files.createDirectories(outputPath);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Unable to set report output directory to: %s", outputDir), e);
        }

        this.outputDir = outputPath;
    }

    public void readSources(TestSourceRead event) {
        sourcesHelper.addSources(event);
    }

    public void addTestCase(TestCaseStarted event) {
        ReportFeature reportFeature = getFeature(event);
        if (sourcesHelper.hasBackground(event)) {
            reportFeature.addElement(sourcesHelper.createBackground(event));
        }
        reportFeature.addElement(sourcesHelper.createTestCase(event));
    }

    public void startStep(TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            addStep(event, sourcesHelper.createTestStep(event));
        } else if (event.getTestStep() instanceof HookTestStep) {
            addHook(event, new HashMap<>());
        } else {
            throw new IllegalStateException("Report generator only supports steps of type PickleStepTestStep and HookTestStep");
        }
    }

    private void addStep(TestStepStarted event, Map<String, Object> entry) {
        ReportFeature reportFeature = getFeature(event);
        if (sourcesHelper.isBackgroundStep(event)) {
            reportFeature.addStep(entry, ReportFeature.StepType.BACKGROUND);
        } else {
            reportFeature.addStep(entry, ReportFeature.StepType.SCENARIO);
        }
    }

    private void addHook(TestStepStarted event, Map<String, Object> entry) {
        ReportFeature reportFeature = getFeature(event);
        HookType hookType = ((HookTestStep) event.getTestStep()).getHookType();
        switch (hookType) {
        case BEFORE:
            reportFeature.addBeforeHook(entry);
            break;
        case AFTER:
            reportFeature.addAfterHook(entry);
            break;
        case BEFORE_STEP:
            reportFeature.addBeforeStepHook(entry);
            break;
        case AFTER_STEP:
            reportFeature.addAfterStepHook(entry);
            break;
        default:
            throw new IllegalStateException("Unexpected hook type: " + hookType);
        }
    }

    public void finishStep(TestStepFinished event) {
        ReportFeature reportFeature = getFeature(event);

        Map<String, Object> matchMap = sourcesHelper.createMatchMap(event.getTestStep(), event.getResult());
        Map<String, Object> resultMap = sourcesHelper.createResultMap(event.getResult());

        reportFeature.finishStep(event.getTestStep(), matchMap, resultMap);
    }

    public void finishTestCase(TestCaseFinished event) {
        ReportFeature feature = getFeature(event);
        Path outputPath = outputDir.resolve(getFeatureKey(event) + ".json");

        try (FileWriter fileWriter = new FileWriter(outputPath.toFile())) {
            gson.toJson(Collections.singletonList(feature.getMap()), fileWriter);
        } catch (IOException e) {
            log.error("Failed to write json feature file.", e);
        }
        reportFeaturesByFileName.remove(getFeatureKey(event));
    }

    public void addText(WriteEvent event) {
        getFeature(event).addText(event);
    }

    public void embed(EmbedEvent event) {
        getFeature(event).embed(event);
    }

    private ReportFeature getFeature(TestCaseEvent event) {
        return reportFeaturesByFileName.computeIfAbsent(getFeatureKey(event), key -> initFeature(event));
    }

    private String getFeatureKey(TestCaseEvent event) {
        return event.getTestCase().getUri().toString().replaceAll("[^a-zA-Z0-9_]+", "_");
    }

    private ReportFeature initFeature(TestCaseEvent event) {

        //we first check if the feature json has already been persisted to disk
        Path featureFile = outputDir.resolve(getFeatureKey(event) + ".json");
        if (featureFile.toFile().exists()) {
            try (FileReader json = new FileReader(featureFile.toFile())) {
                return new ReportFeature((Map<String, Object>) gson.fromJson(json, List.class).get(0));
            } catch (IOException e) {
                throw new IllegalStateException("Could not load json report file", e);
            }
        }
        return new ReportFeature(sourcesHelper.createFeatureMap(event.getTestCase()));
    }
}
