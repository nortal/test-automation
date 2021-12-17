package com.nortal.test.core.plugin

import com.google.gson.Gson
import com.nortal.test.core.plugin.ReportFeature.addElement
import com.nortal.test.core.plugin.ReportFeature.addStep
import com.nortal.test.core.plugin.ReportFeature.addBeforeHook
import com.nortal.test.core.plugin.ReportFeature.addAfterHook
import com.nortal.test.core.plugin.ReportFeature.addBeforeStepHook
import com.nortal.test.core.plugin.ReportFeature.addAfterStepHook
import com.nortal.test.core.plugin.ReportFeature.finishStep
import com.nortal.test.core.plugin.ReportFeature.map
import com.nortal.test.core.plugin.ReportFeature.addText
import com.nortal.test.core.plugin.ReportFeature.embed
import io.cucumber.plugin.event.*
import java.util.HashMap
import java.io.IOException
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.io.FileWriter
import lombok.extern.slf4j.Slf4j
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * This class is responsible for holding all the features from a test suite run.
 * It receives events and routes them to correct features.
 * It also responsible for persisting features to disk when needed.
 */
@Slf4j
class JsonPerFeatureReporter(outputDir: String?) {
    private val outputDir: Path
    private val reportFeaturesByFileName: MutableMap<String, ReportFeature> = HashMap()
    private val sourcesHelper = FeatureSourcesHelper()

    init {
        val outputPath = Paths.get(outputDir)
        try {
            Files.createDirectories(outputPath)
        } catch (e: IOException) {
            throw IllegalArgumentException(String.format("Unable to set report output directory to: %s", outputDir), e)
        }
        this.outputDir = outputPath
    }

    fun readSources(event: TestSourceRead?) {
        sourcesHelper.addSources(event)
    }

    fun addTestCase(event: TestCaseStarted) {
        val reportFeature = getFeature(event)
        if (sourcesHelper.hasBackground(event)) {
            reportFeature.addElement(sourcesHelper.createBackground(event))
        }
        reportFeature.addElement(sourcesHelper.createTestCase(event))
    }

    fun startStep(event: TestStepStarted) {
        if (event.testStep is PickleStepTestStep) {
            addStep(event, sourcesHelper.createTestStep(event))
        } else if (event.testStep is HookTestStep) {
            addHook(event, HashMap())
        } else {
            throw IllegalStateException("Report generator only supports steps of type PickleStepTestStep and HookTestStep")
        }
    }

    private fun addStep(event: TestStepStarted, entry: Map<String, Any>) {
        val reportFeature = getFeature(event)
        if (sourcesHelper.isBackgroundStep(event)) {
            reportFeature.addStep(entry, ReportFeature.StepType.BACKGROUND)
        } else {
            reportFeature.addStep(entry, ReportFeature.StepType.SCENARIO)
        }
    }

    private fun addHook(event: TestStepStarted, entry: Map<String, Any>) {
        val reportFeature = getFeature(event)
        val hookType = (event.testStep as HookTestStep).hookType
        when (hookType) {
            HookType.BEFORE -> reportFeature.addBeforeHook(entry)
            HookType.AFTER -> reportFeature.addAfterHook(entry)
            HookType.BEFORE_STEP -> reportFeature.addBeforeStepHook(entry)
            HookType.AFTER_STEP -> reportFeature.addAfterStepHook(entry)
            else -> throw IllegalStateException("Unexpected hook type: $hookType")
        }
    }

    fun finishStep(event: TestStepFinished) {
        val reportFeature = getFeature(event)
        val matchMap = sourcesHelper.createMatchMap(event.testStep, event.result)
        val resultMap = sourcesHelper.createResultMap(event.result)
        reportFeature.finishStep(event.testStep, matchMap, resultMap)
    }

    fun finishTestCase(event: TestCaseFinished) {
        val feature = getFeature(event)
        val outputPath = outputDir.resolve(getFeatureKey(event) + ".json")
        try {
            FileWriter(outputPath.toFile()).use { fileWriter -> gson.toJson(listOf(feature.map), fileWriter) }
        } catch (e: IOException) {
            JsonPerFeatureReporter.log.error("Failed to write json feature file.", e)
        }
        reportFeaturesByFileName.remove(getFeatureKey(event))
    }

    fun addText(event: WriteEvent) {
        getFeature(event).addText(event)
    }

    fun embed(event: EmbedEvent) {
        getFeature(event).embed(event)
    }

    private fun getFeature(event: TestCaseEvent): ReportFeature {
        return reportFeaturesByFileName.computeIfAbsent(getFeatureKey(event)) { key: String? -> initFeature(event) }
    }

    private fun getFeatureKey(event: TestCaseEvent): String {
        return event.testCase.uri.toString().replace("[^a-zA-Z0-9_]+".toRegex(), "_")
    }

    private fun initFeature(event: TestCaseEvent): ReportFeature {

        //we first check if the feature json has already been persisted to disk
        val featureFile = outputDir.resolve(getFeatureKey(event) + ".json")
        if (featureFile.toFile().exists()) {
            try {
                FileReader(featureFile.toFile()).use { json ->
                    return ReportFeature(
                        gson.fromJson<List<*>>(
                            json,
                            MutableList::class.java
                        )[0] as Map<String?, Any?>?
                    )
                }
            } catch (e: IOException) {
                throw IllegalStateException("Could not load json report file", e)
            }
        }
        return ReportFeature(sourcesHelper.createFeatureMap(event.testCase))
    }

    companion object {
        private val gson = Gson()
    }
}