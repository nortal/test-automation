package com.nortal.test.core.services.report.cucumber

import io.cucumber.testng.Pickle
import io.cucumber.testng.PickleWrapper
import java.util.ArrayList
import java.util.HashMap
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.stream.Collectors

class SkippedScenarios(skippedScenariosByFeatureName: Map<String, MutableList<PickleWrapper>>?) {
    private val skippedScenariosByReportableName: MutableMap<String, MutableList<PickleWrapper>>
    var featureNames: Set<String>

    init {
        skippedScenariosByReportableName = HashMap()
        if (skippedScenariosByFeatureName == null) {
            featureNames = emptySet()
        } else {
            val pickles = skippedScenariosByFeatureName!!.values.stream()
                .flatMap { obj: List<PickleWrapper> -> obj.stream() }
                .collect(Collectors.toList())
            val skippedScenariosByTag = createSkippedScenariosByTag(pickles)
            skippedScenariosByReportableName.putAll(skippedScenariosByFeatureName)
            featureNames = skippedScenariosByFeatureName.keys
            skippedScenariosByReportableName.putAll(skippedScenariosByTag)
        }
    }

    private fun createSkippedScenariosByTag(pickles: List<PickleWrapper>): HashMap<String, MutableList<PickleWrapper>> {
        val skippedScenariosByTag = HashMap<String, MutableList<PickleWrapper>>()
        pickles.forEach(Consumer { pickleWrapper: PickleWrapper ->
            pickleWrapper.pickle.tags.forEach(
                Consumer { tag: String ->
                    skippedScenariosByTag.compute(tag, BiFunction { key: String?, pickleWrappers: MutableList<PickleWrapper>? ->
                        var skippedPickles: MutableList<PickleWrapper> = ArrayList()
                        if (pickleWrappers != null) {
                            skippedPickles = pickleWrappers
                        }
                        skippedPickles.add(pickleWrapper)
                        skippedPickles
                    })
                }
            )
        })
        return skippedScenariosByTag
    }

    fun getScenarioNamesByReportableName(name: String?): List<String> {
        return skippedScenariosByReportableName.getOrDefault(name, emptyList()).stream()
            .map { pickleWrapper: PickleWrapper -> pickleWrapper.pickle.name }
            .collect(Collectors.toList())
    }

    fun getScenariosByReportableName(name: String?): List<Pickle> {
        return skippedScenariosByReportableName.getOrDefault(name, emptyList()).stream()
            .map { obj: PickleWrapper -> obj.pickle }
            .collect(Collectors.toList())
    }

    // Casting to int because we should never have more than 2147483647 scenarios.
    // The tests would never finish.
    val totalScenarioCount: Int
        get() =// Casting to int because we should never have more than 2147483647 scenarios.
            // The tests would never finish.
            skippedScenariosByReportableName.values.stream()
                .flatMap { obj: List<PickleWrapper> -> obj.stream() }
                .distinct()
                .count().toInt()
}