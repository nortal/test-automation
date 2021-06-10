package com.nortal.test.core.plugin;

import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.HookType;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.WriteEvent;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a report in json format for a single feature.
 * It holds state in a tricky way, as currentStepOrHook and lastAddedStep fields keep being reassigned to different maps throughout the test suite run.
 * Such reassignment is needed since embed and add text events do not carry the information about which step they should be added to,
 * thus forcing us to keep track on which the current step is.
 *
 * <p>Other than that cucumber guarantees that events will always come in a specific order that is:
 * <pre>
 * 1. Test case started
 * 2. Step started
 * 3. Step finished
 * 4. repeat of 2 and 3 until all steps are done
 * 5. Test case finished
 * </pre>
 *
 */
public class ReportFeature {
    private Map<String, Object> featureMap;

    private List<Map<String, Object>> elements;
    private Map<String, Object> lastAddedStep;
    private Map<String, Object> currentStepOrHook;

    private List<Map<String, Object>> beforeStepHooks = new ArrayList<>();

    public ReportFeature(Map<String, Object> featureMap) {
        this.featureMap = featureMap;
        this.elements = (List<Map<String, Object>>) featureMap.get("elements");
    }

    public Map<String, Object> getMap() {
        return Collections.unmodifiableMap(featureMap);
    }

    public void addElement(Map<String, Object> entry) {
        elements.add(entry);
    }

    public void addStep(Map<String, Object> entry, StepType type) {
        if (!beforeStepHooks.isEmpty()) {
            ((List<Map<String, Object>>) entry.computeIfAbsent("before", key -> new ArrayList<>())).addAll(beforeStepHooks);
            beforeStepHooks.clear();
        }

        ((List<Map<String, Object>>) getLastElementOfType(type).get("steps")).add(entry);

        currentStepOrHook = entry;
    }

    private Map<String, Object> getLastElementOfType(StepType type) {
        return elements.stream()
                .filter(it -> it.get("type").equals(type.getName()))
                .reduce((a, b) -> b)
                .orElseThrow(() -> new IllegalStateException("Could not find " + type));
    }

    public void finishStep(TestStep testStep, Map<String, Object> matchMap, Map<String, Object> resultMap) {
        currentStepOrHook.put("match", matchMap);
        currentStepOrHook.put("result", resultMap);

        if (testStep instanceof HookTestStep) {
            if (((HookTestStep) testStep).getHookType().equals(HookType.AFTER_STEP)) {
                ((List<Map<String, Object>>) lastAddedStep.computeIfAbsent("after", key -> new ArrayList<>())).add(currentStepOrHook);
            }
        } else {
            lastAddedStep = currentStepOrHook;
        }
        currentStepOrHook = null;
    }

    public void addAfterStepHook(Map<String, Object> entry) {
        currentStepOrHook = entry;
    }

    public void addBeforeStepHook(Map<String, Object> entry) {
        currentStepOrHook = entry;
        beforeStepHooks.add(entry);
    }

    public void addBeforeHook(Map<String, Object> entry) {
        currentStepOrHook = entry;
        getHookMap("before").add(entry);
    }

    public void addAfterHook(Map<String, Object> entry) {
        currentStepOrHook = entry;
        getHookMap("after").add(entry);
    }

    private List<Map<String, Object>> getHookMap(String hookType) {
        return (List<Map<String, Object>>) getLastElementOfType(StepType.SCENARIO).computeIfAbsent(hookType, key -> new ArrayList<>());
    }

    public void addText(WriteEvent event) {
        ((List<String>) currentStepOrHook.computeIfAbsent("output", key -> new ArrayList<>())).add(event.getText());
    }

    public void embed(EmbedEvent event) {
        Map<String, Object> embedMap = new HashMap<>();
        embedMap.put("mime_type", event.getMediaType());
        embedMap.put("data", Base64.getEncoder().encodeToString(event.getData()));
        embedMap.put("name", event.name);
        ((List<Map<String, Object>>) currentStepOrHook.computeIfAbsent("embeddings", key -> new ArrayList<>())).add(embedMap);
    }

    public enum StepType {
        BACKGROUND("background"),
        SCENARIO("scenario");

        private String name;

        StepType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
