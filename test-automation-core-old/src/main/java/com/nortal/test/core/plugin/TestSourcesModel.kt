package com.nortal.test.core.plugin

import io.cucumber.core.gherkin.messages.internal.gherkin.GherkinDocumentBuilder
import io.cucumber.core.gherkin.messages.internal.gherkin.Parser
import io.cucumber.core.gherkin.messages.internal.gherkin.ParserException
import io.cucumber.core.gherkin.messages.internal.gherkin.TokenMatcher
import io.cucumber.messages.IdGenerator
import io.cucumber.messages.Messages
import io.cucumber.plugin.event.TestSourceRead
import java.util.HashMap
import io.cucumber.messages.Messages.GherkinDocument
import io.cucumber.messages.Messages.GherkinDocument.Feature.FeatureChild
import lombok.SneakyThrows
import io.cucumber.messages.Messages.GherkinDocument.Feature.Background
import lombok.extern.slf4j.Slf4j
import java.util.Locale

@Slf4j
class TestSourcesModel {
    private val pathToReadEventMap: MutableMap<String, TestSourceRead> = HashMap()
    private val pathToAstMap: MutableMap<String, GherkinDocument> = HashMap()
    private val pathToNodeMap: MutableMap<String, Map<Int, AstNode>> = HashMap()
    fun addTestSourceReadEvent(path: String, event: TestSourceRead) {
        pathToReadEventMap[path] = event
    }

    fun getFeature(path: String): GherkinDocument.Feature? {
        if (!pathToAstMap.containsKey(path)) {
            parseGherkinSource(path)
        }
        return if (pathToAstMap.containsKey(path)) {
            pathToAstMap[path]!!.feature
        } else null
    }

    fun getAstNode(path: String, line: Int): AstNode? {
        if (!pathToNodeMap.containsKey(path)) {
            parseGherkinSource(path)
        }
        return if (pathToNodeMap.containsKey(path)) {
            pathToNodeMap[path]!![line]
        } else null
    }

    fun hasBackground(path: String, line: Int): Boolean {
        if (!pathToNodeMap.containsKey(path)) {
            parseGherkinSource(path)
        }
        if (pathToNodeMap.containsKey(path)) {
            val astNode = pathToNodeMap[path]!![line]
            return getBackgroundForTestCase(astNode) != null
        }
        return false
    }

    fun getFeatureName(uri: String): String {
        val feature = getFeature(uri)
        return if (feature != null) {
            feature.name
        } else ""
    }

    private fun parseGherkinSource(path: String) {
        if (!pathToReadEventMap.containsKey(path)) {
            return
        }
        val parser: Parser<GherkinDocument.Builder?> = Parser<Any?>(GherkinDocumentBuilder(IdGenerator.UUID()))
        val matcher = TokenMatcher()
        try {
            val gherkinDocument = parser.parse(pathToReadEventMap[path]!!.source, matcher)
            pathToAstMap[path] = gherkinDocument!!.build()
            val nodeMap: MutableMap<Int, AstNode> = HashMap()
            val currentParent: AstNode = AstNode(
                gherkinDocument.feature, null
            )
            for (child in gherkinDocument.feature.childrenList) {
                processScenarioDefinition(nodeMap, child, currentParent)
            }
            pathToNodeMap[path] = nodeMap
        } catch (e: ParserException) {
            TestSourcesModel.log.debug("Error parsing Gherkin source", e)
        }
    }

    private fun processScenarioDefinition(
        nodeMap: MutableMap<Int, AstNode>, child: FeatureChild,
        currentParent: AstNode
    ) {
        val childNode: AstNode = AstNode(child, currentParent)
        nodeMap[getLine(child)] = childNode
        if (isBackgroundStep(childNode)) {
            for (step in child.background.stepsList) {
                nodeMap[step.location.line] = AstNode(step, childNode)
            }
        }
        for (step in child.scenario.stepsList) {
            nodeMap[step.location.line] = AstNode(step, childNode)
        }
        if (child.hasScenario() && child.scenario.keyword.equals("scenario outline", ignoreCase = true)) {
            processScenarioOutlineExamples(nodeMap, child, childNode)
        }
    }

    @SneakyThrows
    private fun getLine(child: FeatureChild): Int {
        val valueField = FeatureChild::class.java.getDeclaredField("value_")
        valueField.isAccessible = true
        val value = valueField[child]
        val locationField = value.javaClass.getDeclaredField("location_")
        locationField.isAccessible = true
        val location = locationField[value] as Messages.Location
        return location.line
    }

    private fun processScenarioOutlineExamples(
        nodeMap: MutableMap<Int, AstNode>, scenarioOutline: FeatureChild,
        childNode: AstNode
    ) {
        for (examples in scenarioOutline.scenario
            .examplesList) {
            val examplesNode: AstNode = AstNode(examples, childNode)
            val headerRow = examples.tableHeader
            val headerNode: AstNode = AstNode(headerRow, examplesNode)
            nodeMap[headerRow.location.line] = headerNode
            for (i in examples.tableBodyList.indices) {
                val examplesRow = examples.getTableBody(i)
                val rowNode: RowNode = RowNode(examplesRow, i)
                val expandedScenarioNode: AstNode = AstNode(rowNode, examplesNode)
                nodeMap[examplesRow.location.line] = expandedScenarioNode
            }
        }
    }

    internal inner class RowNode(val row: GherkinDocument.Feature.TableRow, val bodyRowIndex: Int)
    inner class AstNode(val node: Any, val parent: AstNode?)
    companion object {
        fun getFeatureForTestCase(astNode: AstNode?): GherkinDocument.Feature {
            var astNode = astNode
            while (astNode!!.parent != null) {
                astNode = astNode.parent
            }
            return astNode.node as GherkinDocument.Feature
        }

        @JvmStatic
        fun getBackgroundForTestCase(astNode: AstNode?): Background? {
            val feature = getFeatureForTestCase(astNode)
            val background = feature.getChildren(0)
            return if (background.hasBackground()) {
                background.background
            } else {
                null
            }
        }

        @JvmStatic
        fun getScenarioDefinition(astNode: AstNode): GherkinDocument.Feature.Scenario {
            return if (astNode.node is FeatureChild) astNode.node.scenario else (astNode.parent!!.parent!!.node as FeatureChild).scenario
        }

        @JvmStatic
        fun isBackgroundStep(astNode: AstNode): Boolean {
            return astNode.node is FeatureChild && astNode.node.hasBackground() //return astNode.parent.node instanceof Background;
        }

        @JvmStatic
        fun calculateId(astNode: AstNode?): String {
            val node = astNode?.node
            if (node is GherkinDocument.Feature.Scenario) {
                return calculateId(astNode.parent) + ";" + convertToId(
                    node.name
                )
            }
            if (node is RowNode) {
                return calculateId(astNode.parent) + ";" + (node.bodyRowIndex + 2)
            }
            if (node is GherkinDocument.Feature.TableRow) {
                return calculateId(astNode.parent) + ";" + 1
            }
            if (node is GherkinDocument.Feature.Scenario.Examples) {
                return calculateId(astNode.parent) + ";" + convertToId(
                    node.name
                )
            }
            if (node is GherkinDocument.Feature) {
                return calculateId(astNode.parent) + ";" + convertToId(
                    node.name
                )
            }
            return if (node is FeatureChild) {
                calculateId(astNode.parent) + ";" + convertToId(
                    node.scenario.name
                )
            } else ""
        }

        @JvmStatic
        fun convertToId(name: String): String {
            return name.replace("[\\s'_,!]".toRegex(), "-").lowercase(Locale.getDefault())
        }
    }
}