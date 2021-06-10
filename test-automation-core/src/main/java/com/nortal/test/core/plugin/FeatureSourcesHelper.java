package com.nortal.test.core.plugin;

import io.cucumber.messages.Messages;
import io.cucumber.plugin.event.DataTableArgument;
import io.cucumber.plugin.event.DocStringArgument;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.StepArgument;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestSourceRead;
import io.cucumber.plugin.event.TestStep;
import io.cucumber.plugin.event.TestStepStarted;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class helps with parsing cucumber feature files and creating json structure of the report.
 * Most of the methods here and in TestSourcesModel class are lifted from cucumber source as is.
 */
public class FeatureSourcesHelper {
    private static final String STEPS = "steps";
    private static final String KEYWORD = "keyword";
    private static final String DESCRIPTION = "description";

    private final TestSourcesModel testSources = new TestSourcesModel();

    public void addSources(TestSourceRead event) {
        testSources.addTestSourceReadEvent(event.getUri().toString(), event);
    }

    public String getFeatureName(String uri) {
        return testSources.getFeatureName(uri);
    }

    public boolean hasBackground(TestCaseStarted event) {
        return testSources.hasBackground(event.getTestCase().getUri().toString(), event.getTestCase().getLine());
    }

    public boolean isBackgroundStep(TestStepStarted step) {
        TestSourcesModel.AstNode astNode = testSources.getAstNode(step.getTestCase().getUri().toString(), ((PickleStepTestStep) step.getTestStep()).getStepLine());
        return TestSourcesModel.isBackgroundStep(astNode.parent);
    }

    public Map<String, Object> createFeatureMap(TestCase testCase) {
        Map<String, Object> featureMap = new HashMap<>();
        featureMap.put("uri", testCase.getUri());
        featureMap.put("elements", new ArrayList<Map<String, Object>>());
        Messages.GherkinDocument.Feature feature = testSources.getFeature(testCase.getUri().toString());
        if (feature != null) {
            featureMap.put(KEYWORD, feature.getKeyword());
            featureMap.put("name", feature.getName());
            featureMap.put(DESCRIPTION, feature.getDescription() != null ? feature.getDescription() : "");
            featureMap.put("line", feature.getLocation().getLine());
            featureMap.put("id", TestSourcesModel.convertToId(feature.getName()));
            featureMap.put("tags", transformTags(feature));
        }
        return featureMap;
    }

    private List<Map<String, Object>> transformTags(final Messages.GherkinDocument.Feature feature) {
        return feature
            .getTagsList()
            .stream()
            .map(tag -> Map.of("name", tag.getName(), "type", "Tag", "location",
                Map.of("line", tag.getLocation().getLine(), "column", tag.getLocation().getColumn())
            ))
            .collect(Collectors.toList());
    }

    public Map<String, Object> createMatchMap(TestStep step, Result result) {
        Map<String, Object> matchMap = new HashMap<>();
        if (step instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) step;
            if (!testStep.getDefinitionArgument().isEmpty()) {
                List<Map<String, Object>> argumentList = new ArrayList<>();
                for (io.cucumber.plugin.event.Argument argument : testStep.getDefinitionArgument()) {
                    Map<String, Object> argumentMap = new HashMap<>();
                    if (argument.getValue() != null) {
                        argumentMap.put("val", argument.getValue());
                        argumentMap.put("offset", argument.getStart());
                    }
                    argumentList.add(argumentMap);
                }
                matchMap.put("arguments", argumentList);
            }
        }
        if (!result.getStatus().is(Status.UNDEFINED)) {
            matchMap.put("location", step.getCodeLocation());
        }
        return matchMap;
    }

    public Map<String, Object> createResultMap(Result result) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", result.getStatus().toString());
        if (result.getError() != null) {
            resultMap.put("error_message", result.getError().getMessage());
        }
        if (result.getDuration() != null && !result.getDuration().isZero()) {
            resultMap.put("duration", result.getDuration().toNanos());
        }
        return resultMap;
    }

    public Map<String, Object> createTestCase(TestCaseStarted event) {
        TestCase testCase = event.getTestCase();
        Map<String, Object> testCaseMap = new HashMap<>();
        testCaseMap.put("name", testCase.getName());
        testCaseMap.put("line", testCase.getLine());
        testCaseMap.put("type", "scenario");
        TestSourcesModel.AstNode astNode = testSources.getAstNode(testCase.getUri().toString(), testCase.getLine());
        if (astNode != null) {
            testCaseMap.put("id", TestSourcesModel.calculateId(astNode));
            Messages.GherkinDocument.Feature.Scenario scenarioDefinition =
                TestSourcesModel.getScenarioDefinition(astNode);
            testCaseMap.put(KEYWORD, scenarioDefinition.getKeyword());
            testCaseMap.put(DESCRIPTION, scenarioDefinition.getDescription() != null
                                         ? scenarioDefinition.getDescription()
                                         : "");
        }
        testCaseMap.put(STEPS, new ArrayList<Map<String, Object>>());
        if (!testCase.getTags().isEmpty()) {
            List<Map<String, Object>> tagList = new ArrayList<>();
            for (String tag : testCase.getTags()) {
                Map<String, Object> tagMap = new HashMap<>();
                tagMap.put("name", tag);
                tagList.add(tagMap);
            }
            testCaseMap.put("tags", tagList);
        }
        return testCaseMap;
    }

    public Map<String, Object> createBackground(TestCaseStarted event) {
        TestCase testCase = event.getTestCase();
        TestSourcesModel.AstNode astNode = testSources.getAstNode(testCase.getUri().toString(), testCase.getLine());
        if (astNode != null) {
            Messages.GherkinDocument.Feature.Background background = TestSourcesModel.getBackgroundForTestCase(astNode);
            Map<String, Object> testCaseMap = new HashMap<>();
            testCaseMap.put("name", background.getName());
            testCaseMap.put("line", background.getLocation().getLine());
            testCaseMap.put("type", "background");
            testCaseMap.put(KEYWORD, background.getKeyword());
            testCaseMap.put(DESCRIPTION, background.getDescription() != null ? background.getDescription() : "");
            testCaseMap.put(STEPS, new ArrayList<Map<String, Object>>());
            return testCaseMap;
        }
        return null;
    }

    public Map<String, Object> createTestStep(TestStepStarted testStepEvent) {
        io.cucumber.plugin.event.Step testStep = ((PickleStepTestStep) testStepEvent.getTestStep()).getStep();
        Map<String, Object> stepMap = new HashMap<>();
        stepMap.put("name", testStep.getText());
        stepMap.put("line", testStep.getLine());
        TestSourcesModel.AstNode astNode = testSources.getAstNode(testStepEvent.getTestCase().getUri().toString(), testStep.getLine());
        if (testStep.getArgument() != null) {
            StepArgument argument = testStep.getArgument();
            if (argument instanceof DocStringArgument) {
                stepMap.put("doc_string", createDocStringMap(argument));
            } else if (argument instanceof DataTableArgument) {
                stepMap.put("rows", createDataTableList(argument));
            }
        }
        if (astNode != null) {
            Messages.GherkinDocument.Feature.Step step = (Messages.GherkinDocument.Feature.Step) astNode.node;
            stepMap.put(KEYWORD, step.getKeyword());
        }

        return stepMap;
    }

    private Map<String, Object> createDocStringMap(StepArgument argument) {
        Map<String, Object> docStringMap = new HashMap<>();
        DocStringArgument docString = (DocStringArgument) argument;
        docStringMap.put("value", docString.getContent());
        docStringMap.put("line", docString.getLine());
        docStringMap.put("content_type", docString.getContentType());
        return docStringMap;
    }

    private List<Map<String, List<String>>> createDataTableList(StepArgument argument) {
        List<Map<String, List<String>>> rowList = new ArrayList<>();
        for (List<String> row : ((DataTableArgument)argument).cells()) {
            Map<String, List<String>> rowMap = new HashMap<>();
            rowMap.put("cells", new ArrayList<>(row));
            rowList.add(rowMap);
        }
        return rowList;
    }

}
