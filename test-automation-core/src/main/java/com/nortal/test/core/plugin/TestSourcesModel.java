package com.nortal.test.core.plugin;

import java.util.HashMap;
import java.util.Map;
import io.cucumber.core.internal.gherkin.AstBuilder;
import io.cucumber.core.internal.gherkin.Parser;
import io.cucumber.core.internal.gherkin.ParserException;
import io.cucumber.core.internal.gherkin.TokenMatcher;
import io.cucumber.core.internal.gherkin.ast.Background;
import io.cucumber.core.internal.gherkin.ast.Examples;
import io.cucumber.core.internal.gherkin.ast.Feature;
import io.cucumber.core.internal.gherkin.ast.GherkinDocument;
import io.cucumber.core.internal.gherkin.ast.Node;
import io.cucumber.core.internal.gherkin.ast.ScenarioDefinition;
import io.cucumber.core.internal.gherkin.ast.ScenarioOutline;
import io.cucumber.core.internal.gherkin.ast.Step;
import io.cucumber.core.internal.gherkin.ast.TableRow;
import io.cucumber.plugin.event.TestSourceRead;

public class TestSourcesModel {

    private final Map<String, TestSourceRead> pathToReadEventMap = new HashMap<>();
    private final Map<String, GherkinDocument> pathToAstMap = new HashMap<>();
    private final Map<String, Map<Integer, AstNode>> pathToNodeMap = new HashMap<>();

    static Feature getFeatureForTestCase(TestSourcesModel.AstNode astNode) {
        while (astNode.parent != null) {
            astNode = astNode.parent;
        }
        return (Feature) astNode.node;
    }

    static Background getBackgroundForTestCase(TestSourcesModel.AstNode astNode) {
        Feature feature = getFeatureForTestCase(astNode);
        ScenarioDefinition backgound = feature.getChildren().get(0);
        if (backgound instanceof Background) {
            return (Background) backgound;
        } else {
            return null;
        }
    }

    static ScenarioDefinition getScenarioDefinition(TestSourcesModel.AstNode astNode) {
        return astNode.node instanceof ScenarioDefinition ? (ScenarioDefinition) astNode.node : (ScenarioDefinition) astNode.parent.parent.node;
    }

    static boolean isBackgroundStep(TestSourcesModel.AstNode astNode) {
        return astNode.parent.node instanceof Background;
    }

    static String calculateId(TestSourcesModel.AstNode astNode) {
        Node node = astNode.node;
        if (node instanceof ScenarioDefinition) {
            return calculateId(astNode.parent) + ";" + convertToId(((ScenarioDefinition) node).getName());
        }
        if (node instanceof TestSourcesModel.ExamplesRowWrapperNode) {
            return calculateId(astNode.parent) + ";" + (((ExamplesRowWrapperNode) node).bodyRowIndex + 2);
        }
        if (node instanceof TableRow) {
            return calculateId(astNode.parent) + ";" + 1;
        }
        if (node instanceof Examples) {
            return calculateId(astNode.parent) + ";" + convertToId(((Examples) node).getName());
        }
        if (node instanceof Feature) {
            return convertToId(((Feature) node).getName());
        }
        return "";
    }

    static String convertToId(String name) {
        return name.replaceAll("[\\s'_,!]", "-").toLowerCase();
    }

    public void addTestSourceReadEvent(String path, TestSourceRead event) {
        pathToReadEventMap.put(path, event);
    }

    public Feature getFeature(String path) {
        if (!pathToAstMap.containsKey(path)) {
            parseGherkinSource(path);
        }
        if (pathToAstMap.containsKey(path)) {
            return pathToAstMap.get(path).getFeature();
        }
        return null;
    }

    TestSourcesModel.AstNode getAstNode(String path, int line) {
        if (!pathToNodeMap.containsKey(path)) {
            parseGherkinSource(path);
        }
        if (pathToNodeMap.containsKey(path)) {
            return pathToNodeMap.get(path).get(line);
        }
        return null;
    }

    boolean hasBackground(String path, int line) {
        if (!pathToNodeMap.containsKey(path)) {
            parseGherkinSource(path);
        }
        if (pathToNodeMap.containsKey(path)) {
            TestSourcesModel.AstNode astNode = pathToNodeMap.get(path).get(line);
            return getBackgroundForTestCase(astNode) != null;
        }
        return false;
    }

    public String getFeatureName(String uri) {
        Feature feature = getFeature(uri);
        if (feature != null) {
            return feature.getName();
        }
        return "";
    }

    private void parseGherkinSource(String path) {
        if (!pathToReadEventMap.containsKey(path)) {
            return;
        }
        Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
        TokenMatcher matcher = new TokenMatcher();
        try {
            GherkinDocument gherkinDocument = parser.parse(pathToReadEventMap.get(path).getSource(), matcher);
            pathToAstMap.put(path, gherkinDocument);
            Map<Integer, AstNode> nodeMap = new HashMap<>();
            TestSourcesModel.AstNode currentParent = new TestSourcesModel.AstNode(gherkinDocument.getFeature(), null);
            for (ScenarioDefinition child : gherkinDocument.getFeature().getChildren()) {
                processScenarioDefinition(nodeMap, child, currentParent);
            }
            pathToNodeMap.put(path, nodeMap);
        } catch (ParserException e) {
            // Ignore exceptions
        }
    }

    private void processScenarioDefinition(Map<Integer, AstNode> nodeMap, ScenarioDefinition child,
            TestSourcesModel.AstNode currentParent) {
        TestSourcesModel.AstNode childNode = new TestSourcesModel.AstNode(child, currentParent);
        nodeMap.put(child.getLocation().getLine(), childNode);
        for (Step step : child.getSteps()) {
            nodeMap.put(step.getLocation().getLine(), new TestSourcesModel.AstNode(step, childNode));
        }
        if (child instanceof ScenarioOutline) {
            processScenarioOutlineExamples(nodeMap, (ScenarioOutline) child, childNode);
        }
    }

    private void processScenarioOutlineExamples(Map<Integer, AstNode> nodeMap, ScenarioOutline scenarioOutline,
            TestSourcesModel.AstNode childNode) {
        for (Examples examples : scenarioOutline.getExamples()) {
            TestSourcesModel.AstNode examplesNode = new TestSourcesModel.AstNode(examples, childNode);
            TableRow headerRow = examples.getTableHeader();
            TestSourcesModel.AstNode headerNode = new TestSourcesModel.AstNode(headerRow, examplesNode);
            nodeMap.put(headerRow.getLocation().getLine(), headerNode);
            for (int i = 0; i < examples.getTableBody().size(); ++i) {
                TableRow examplesRow = examples.getTableBody().get(i);
                Node rowNode = new TestSourcesModel.ExamplesRowWrapperNode(examplesRow, i);
                TestSourcesModel.AstNode expandedScenarioNode = new TestSourcesModel.AstNode(rowNode, examplesNode);
                nodeMap.put(examplesRow.getLocation().getLine(), expandedScenarioNode);
            }
        }
    }

    class ExamplesRowWrapperNode extends Node {
        final int bodyRowIndex;

        ExamplesRowWrapperNode(Node examplesRow, int bodyRowIndex) {
            super(examplesRow.getLocation());
            this.bodyRowIndex = bodyRowIndex;
        }
    }

    class AstNode {
        final Node node;
        final TestSourcesModel.AstNode parent;

        AstNode(Node node, TestSourcesModel.AstNode parent) {
            this.node = node;
            this.parent = parent;
        }
    }
}

