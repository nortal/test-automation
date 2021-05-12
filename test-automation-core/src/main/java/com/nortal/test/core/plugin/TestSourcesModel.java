package com.nortal.test.core.plugin;

import io.cucumber.core.gherkin.messages.internal.gherkin.GherkinDocumentBuilder;
import io.cucumber.core.gherkin.messages.internal.gherkin.Parser;
import io.cucumber.core.gherkin.messages.internal.gherkin.ParserException;
import io.cucumber.core.gherkin.messages.internal.gherkin.TokenMatcher;
import io.cucumber.messages.IdGenerator;
import io.cucumber.messages.Messages;
import io.cucumber.plugin.event.TestSourceRead;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TestSourcesModel {

    private final Map<String, TestSourceRead> pathToReadEventMap = new HashMap<>();
    private final Map<String, Messages.GherkinDocument> pathToAstMap = new HashMap<>();
    private final Map<String, Map<Integer, AstNode>> pathToNodeMap = new HashMap<>();

    static Messages.GherkinDocument.Feature getFeatureForTestCase(TestSourcesModel.AstNode astNode) {
        while (astNode.parent != null) {
            astNode = astNode.parent;
        }
        return (Messages.GherkinDocument.Feature) astNode.node;
    }

    static Messages.GherkinDocument.Feature.Background getBackgroundForTestCase(TestSourcesModel.AstNode astNode) {
        Messages.GherkinDocument.Feature feature = getFeatureForTestCase(astNode);
        final Messages.GherkinDocument.Feature.FeatureChild background = feature.getChildren(0);
        if (background.hasBackground()) {
            return background.getBackground();
        } else {
            return null;
        }
    }

    static Messages.GherkinDocument.Feature.Scenario getScenarioDefinition(TestSourcesModel.AstNode astNode) {
        return astNode.node instanceof Messages.GherkinDocument.Feature.FeatureChild
               ? ((Messages.GherkinDocument.Feature.FeatureChild) astNode.node).getScenario()
               : ((Messages.GherkinDocument.Feature.FeatureChild) astNode.parent.parent.node).getScenario();
    }

    static boolean isBackgroundStep(TestSourcesModel.AstNode astNode) {
        return astNode.node instanceof Messages.GherkinDocument.Feature.FeatureChild && ((Messages.GherkinDocument.Feature.FeatureChild) astNode.node).hasBackground();//return astNode.parent.node instanceof Background;
    }

    static String calculateId(TestSourcesModel.AstNode astNode) {
        Object node = astNode != null ? astNode.node : null;
        if (node instanceof Messages.GherkinDocument.Feature.Scenario) {
            return calculateId(astNode.parent) + ";" + convertToId(((Messages.GherkinDocument.Feature.Scenario) node).getName());
        }
        if (node instanceof TestSourcesModel.RowNode) {
            return calculateId(astNode.parent) + ";" + (((RowNode) node).bodyRowIndex + 2);
        }
        if (node instanceof Messages.GherkinDocument.Feature.TableRow) {
            return calculateId(astNode.parent) + ";" + 1;
        }
        if (node instanceof Messages.GherkinDocument.Feature.Scenario.Examples) {
            return calculateId(astNode.parent) + ";" + convertToId(((Messages.GherkinDocument.Feature.Scenario.Examples) node).getName());
        }
        if (node instanceof Messages.GherkinDocument.Feature) {
            return calculateId(astNode.parent) + ";" + convertToId(((Messages.GherkinDocument.Feature) node).getName());
        }
        if (node instanceof Messages.GherkinDocument.Feature.FeatureChild) {
            return calculateId(astNode.parent) + ";" + convertToId(((Messages.GherkinDocument.Feature.FeatureChild) node).getScenario().getName());
        }
        return "";
    }

    static String convertToId(String name) {
        return name.replaceAll("[\\s'_,!]", "-").toLowerCase();
    }

    public void addTestSourceReadEvent(String path, TestSourceRead event) {
        pathToReadEventMap.put(path, event);
    }

    public Messages.GherkinDocument.Feature getFeature(String path) {
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
        Messages.GherkinDocument.Feature feature = getFeature(uri);
        if (feature != null) {
            return feature.getName();
        }
        return "";
    }

    private void parseGherkinSource(String path) {
        if (!pathToReadEventMap.containsKey(path)) {
            return;
        }
        Parser<Messages.GherkinDocument.Builder> parser =
            new Parser(new GherkinDocumentBuilder(new IdGenerator.UUID()));
        TokenMatcher matcher = new TokenMatcher();
        try {
            Messages.GherkinDocument.Builder gherkinDocument =
                parser.parse(pathToReadEventMap.get(path).getSource(), matcher);
            pathToAstMap.put(path, gherkinDocument.build());
            Map<Integer, AstNode> nodeMap = new HashMap<>();
            TestSourcesModel.AstNode currentParent = new TestSourcesModel.AstNode(gherkinDocument.getFeature(), null);
            for (Messages.GherkinDocument.Feature.FeatureChild child : gherkinDocument.getFeature().getChildrenList()) {
                processScenarioDefinition(nodeMap, child, currentParent);
            }
            pathToNodeMap.put(path, nodeMap);
        } catch (ParserException e) {
            log.debug("Error parsing Gherkin source", e);
        }
    }

    private void processScenarioDefinition(
        final Map<Integer, AstNode> nodeMap, final Messages.GherkinDocument.Feature.FeatureChild child,
        final TestSourcesModel.AstNode currentParent
    ) {
        final TestSourcesModel.AstNode childNode = new TestSourcesModel.AstNode(child, currentParent);
        nodeMap.put(getLine(child), childNode);

        if (isBackgroundStep(childNode)) {
            for (Messages.GherkinDocument.Feature.Step step :  child.getBackground().getStepsList()) {
                nodeMap.put(step.getLocation().getLine(), new TestSourcesModel.AstNode(step, childNode));
            }
        }

        for (Messages.GherkinDocument.Feature.Step step : child.getScenario().getStepsList()) {
            nodeMap.put(step.getLocation().getLine(), new TestSourcesModel.AstNode(step, childNode));
        }
        if (child.hasScenario() && child.getScenario().getKeyword().equalsIgnoreCase("scenario outline")) {
            processScenarioOutlineExamples(nodeMap, child, childNode);
        }
    }

    @SneakyThrows
    private int getLine(final Messages.GherkinDocument.Feature.FeatureChild child) {
        final Field valueField = Messages.GherkinDocument.Feature.FeatureChild.class.getDeclaredField("value_");
        valueField.setAccessible(true);
        final Object value = valueField.get(child);

        final Field locationField = value.getClass().getDeclaredField("location_");
        locationField.setAccessible(true);
        final Messages.Location location = (Messages.Location) locationField.get(value);

        return location.getLine();
    }

    private void processScenarioOutlineExamples(
        Map<Integer, AstNode> nodeMap, Messages.GherkinDocument.Feature.FeatureChild scenarioOutline,
        TestSourcesModel.AstNode childNode
    ) {
        for (Messages.GherkinDocument.Feature.Scenario.Examples examples : scenarioOutline.getScenario()
            .getExamplesList()) {
            TestSourcesModel.AstNode examplesNode = new TestSourcesModel.AstNode(examples, childNode);
            Messages.GherkinDocument.Feature.TableRow headerRow = examples.getTableHeader();
            TestSourcesModel.AstNode headerNode = new TestSourcesModel.AstNode(headerRow, examplesNode);
            nodeMap.put(headerRow.getLocation().getLine(), headerNode);

            for (int i = 0; i < examples.getTableBodyList().size(); ++i) {
                Messages.GherkinDocument.Feature.TableRow examplesRow = examples.getTableBody(i);
                RowNode rowNode = new RowNode(examplesRow, i);
                TestSourcesModel.AstNode expandedScenarioNode = new TestSourcesModel.AstNode(rowNode, examplesNode);
                nodeMap.put(examplesRow.getLocation().getLine(), expandedScenarioNode);
            }
        }
    }

    class RowNode {
        final int bodyRowIndex;
        final Messages.GherkinDocument.Feature.TableRow row;

        RowNode(Messages.GherkinDocument.Feature.TableRow row, int bodyRowIndex) {
            this.row = row;
            this.bodyRowIndex = bodyRowIndex;
        }
    }

    class AstNode {
        final Object node;
        final TestSourcesModel.AstNode parent;

        AstNode(Object node, TestSourcesModel.AstNode parent) {
            this.node = node;
            this.parent = parent;
        }
    }
}

