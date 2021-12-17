package com.nortal.test.core.services.report

import com.nortal.test.core.model.Tags
import com.nortal.test.core.services.report.cucumber.SkippedScenarios
import io.cucumber.testng.PickleWrapper
import java.util.HashMap
import io.cucumber.testng.FeatureWrapper
import io.cucumber.testng.Pickle
import org.apache.commons.lang3.RegExUtils
import org.springframework.stereotype.Component
import java.util.ArrayList
import java.util.function.BiFunction

/**
 * Tracks skipped scenarios so that they could be added to the report later on.
 */
@Component
class ScenarioSkipService {
    private val skippedScenariosByFeatureName: MutableMap<String, MutableList<PickleWrapper>> = HashMap()
    fun collectSkippedScenarios(feature: FeatureWrapper?, pickle: PickleWrapper?) {
        if (feature == null || pickle == null) {
            return
        }
        val tags = pickle.pickle.tags
        if (tags.stream().noneMatch { anObject: String? -> Tags.SKIP.name.equals(anObject) }) {
            return
        }

        // FeatureWrapperImpl wraps the name of the feature in double quotes.
        // These get in the way later so we remove them.
        val featureName = RegExUtils.removePattern(feature.toString(), "^\"|\"$")
        skippedScenariosByFeatureName.compute(featureName, BiFunction { name: String?, pickleWrappers: MutableList<PickleWrapper>? ->
            var skippedPickles: MutableList<PickleWrapper> = ArrayList()
            if (pickleWrappers != null) {
                skippedPickles = pickleWrappers
            }
            if (isNotPresent(pickle, skippedPickles)) {
                skippedPickles.add(pickle)
            }
            skippedPickles
        })
    }

    private fun isNotPresent(pickle: PickleWrapper, skippedPickles: List<PickleWrapper>): Boolean {
        val pickleName = pickle.pickle.name
        return skippedPickles.stream()
            .map { obj: PickleWrapper -> obj.pickle }
            .map { obj: Pickle -> obj.name }
            .noneMatch { anObject: String? -> pickleName.equals(anObject) }
    }

    val skippedScenarios: SkippedScenarios
        get() = SkippedScenarios(skippedScenariosByFeatureName)
}