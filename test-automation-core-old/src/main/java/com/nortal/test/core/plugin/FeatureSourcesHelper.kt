package com.nortal.test.core.plugin

import com.nortal.test.core.plugin.TestSourcesModel.addTestSourceReadEvent
import com.nortal.test.core.plugin.TestSourcesModel.getFeatureName
import com.nortal.test.core.plugin.TestSourcesModel.hasBackground
import com.nortal.test.core.plugin.TestSourcesModel.getAstNode
import com.nortal.test.core.plugin.TestSourcesModel.Companion.isBackgroundStep
import com.nortal.test.core.plugin.TestSourcesModel.getFeature
import com.nortal.test.core.plugin.TestSourcesModel.Companion.convertToId
import com.nortal.test.core.plugin.TestSourcesModel.Companion.calculateId
import com.nortal.test.core.plugin.TestSourcesModel.Companion.getScenarioDefinition
import com.nortal.test.core.plugin.TestSourcesModel.Companion.getBackgroundForTestCase
import java.util.HashMap
import io.cucumber.messages.Messages.GherkinDocument
import io.cucumber.messages.Messages.GherkinDocument.Feature.Background
import io.cucumber.plugin.event.*
import java.util.ArrayList
import java.util.stream.Collectors

/**
 * This class helps with parsing cucumber feature files and creating json structure of the report.
 * Most of the methods here and in TestSourcesModel class are lifted from cucumber source as is.
 */
class FeatureSourcesHelper {
    private val testSources = TestSourcesModel()
    fun addSources(event: TestSourceRead) {
        testSources.addTestSourceReadEvent(event.uri.toString(), event)
    }

    fun getFeatureName(uri: String?): String {
        return testSources.getFeatureName(uri!!)
    }

    fun hasBackground(event: TestCaseStarted): Boolean {
        return testSources.hasBackground(event.testCase.uri.toString(), event.testCase.line)
    }

    fun isBackgroundStep(step: TestStepStarted): Boolean {
        val astNode = testSources.getAstNode(step.testCase.uri.toString(), (step.testStep as PickleStepTestStep).stepLine)
        return isBackgroundStep(astNode!!.parent!!)
    }

    fun createFeatureMap(testCase: TestCase): Map<String, Any> {
        val featureMap: MutableMap<String, Any> = HashMap()
        featureMap["uri"] = testCase.uri
        featureMap["elements"] = ArrayList<Map<String, Any>>()
        val feature = testSources.getFeature(testCase.uri.toString())
        if (feature != null) {
            featureMap[KEYWORD] = feature.keyword
            featureMap["name"] = feature.name
            featureMap[DESCRIPTION] = if (feature.description != null) feature.description else ""
            featureMap["line"] = feature.location.line
            featureMap["id"] = convertToId(feature.name)
            featureMap["tags"] = transformTags(feature)
        }
        return featureMap
    }

    private fun transformTags(feature: GherkinDocument.Feature): List<Map<String, Any>> {
        return feature
            .tagsList
            .stream()
            .map { tag: GherkinDocument.Feature.Tag ->
                java.util.Map.of(
                    "name", tag.name, "type", "Tag", "location",
                    java.util.Map.of("line", tag.location.line, "column", tag.location.column)
                )
            }
            .collect(Collectors.toList())
    }

    fun createMatchMap(step: TestStep, result: Result): Map<String, Any> {
        val matchMap: MutableMap<String, Any> = HashMap()
        if (step is PickleStepTestStep) {
            val testStep = step
            if (!testStep.definitionArgument.isEmpty()) {
                val argumentList: MutableList<Map<String, Any>> = ArrayList()
                for (argument in testStep.definitionArgument) {
                    val argumentMap: MutableMap<String, Any> = HashMap()
                    if (argument.value != null) {
                        argumentMap["val"] = argument.value
                        argumentMap["offset"] = argument.start
                    }
                    argumentList.add(argumentMap)
                }
                matchMap["arguments"] = argumentList
            }
        }
        if (!result.status.`is`(Status.UNDEFINED)) {
            matchMap["location"] = step.codeLocation
        }
        return matchMap
    }

    fun createResultMap(result: Result): Map<String, Any?> {
        val resultMap: MutableMap<String, Any?> = HashMap()
        resultMap["status"] = result.status.toString()
        if (result.error != null) {
            resultMap["error_message"] = result.error.message
        }
        if (result.duration != null && !result.duration.isZero) {
            resultMap["duration"] = result.duration.toNanos()
        }
        return resultMap
    }

    fun createTestCase(event: TestCaseStarted): Map<String, Any> {
        val testCase = event.testCase
        val testCaseMap: MutableMap<String, Any> = HashMap()
        testCaseMap["name"] = testCase.name
        testCaseMap["line"] = testCase.line
        testCaseMap["type"] = "scenario"
        val astNode = testSources.getAstNode(testCase.uri.toString(), testCase.line)
        if (astNode != null) {
            testCaseMap["id"] = calculateId(astNode)
            val scenarioDefinition = getScenarioDefinition(astNode)
            testCaseMap[KEYWORD] = scenarioDefinition.keyword
            testCaseMap[DESCRIPTION] = if (scenarioDefinition.description != null) scenarioDefinition.description else ""
        }
        testCaseMap[STEPS] = ArrayList<Map<String, Any>>()
        if (!testCase.tags.isEmpty()) {
            val tagList: MutableList<Map<String, Any>> = ArrayList()
            for (tag in testCase.tags) {
                val tagMap: MutableMap<String, Any> = HashMap()
                tagMap["name"] = tag
                tagList.add(tagMap)
            }
            testCaseMap["tags"] = tagList
        }
        return testCaseMap
    }

    fun createBackground(event: TestCaseStarted): Map<String, Any>? {
        val testCase = event.testCase
        val astNode = testSources.getAstNode(testCase.uri.toString(), testCase.line)
        if (astNode != null) {
            val background = getBackgroundForTestCase(astNode)
            val testCaseMap: MutableMap<String, Any> = HashMap()
            testCaseMap["name"] = background!!.name
            testCaseMap["line"] = background.location.line
            testCaseMap["type"] = "background"
            testCaseMap[KEYWORD] = background.keyword
            testCaseMap[DESCRIPTION] = if (background.description != null) background.description else ""
            testCaseMap[STEPS] = ArrayList<Map<String, Any>>()
            return testCaseMap
        }
        return null
    }

    fun createTestStep(testStepEvent: TestStepStarted): Map<String, Any> {
        val testStep = (testStepEvent.testStep as PickleStepTestStep).step
        val stepMap: MutableMap<String, Any> = HashMap()
        stepMap["name"] = testStep.text
        stepMap["line"] = testStep.line
        val astNode = testSources.getAstNode(testStepEvent.testCase.uri.toString(), testStep.line)
        if (testStep.argument != null) {
            val argument = testStep.argument
            if (argument is DocStringArgument) {
                stepMap["doc_string"] = createDocStringMap(argument)
            } else if (argument is DataTableArgument) {
                stepMap["rows"] = createDataTableList(argument)
            }
        }
        if (astNode != null) {
            val step = astNode.node as GherkinDocument.Feature.Step
            stepMap[KEYWORD] = step.keyword
        }
        return stepMap
    }

    private fun createDocStringMap(argument: StepArgument): Map<String, Any> {
        val docStringMap: MutableMap<String, Any> = HashMap()
        val docString = argument as DocStringArgument
        docStringMap["value"] = docString.content
        docStringMap["line"] = docString.line
        docStringMap["content_type"] = docString.contentType
        return docStringMap
    }

    private fun createDataTableList(argument: StepArgument): List<Map<String, List<String>>> {
        val rowList: MutableList<Map<String, List<String>>> = ArrayList()
        for (row in (argument as DataTableArgument).cells()) {
            val rowMap: MutableMap<String, List<String>> = HashMap()
            rowMap["cells"] = ArrayList(row)
            rowList.add(rowMap)
        }
        return rowList
    }

    companion object {
        private const val STEPS = "steps"
        private const val KEYWORD = "keyword"
        private const val DESCRIPTION = "description"
    }
}