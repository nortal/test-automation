package com.nortal.test.core.services.report.cucumber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import io.cucumber.testng.Pickle;
import io.cucumber.testng.PickleWrapper;

public class SkippedScenarios {
    private final Map<String, List<PickleWrapper>> skippedScenariosByReportableName;
    private final Set<String> featureNames;

    public SkippedScenarios(final Map<String, List<PickleWrapper>> skippedScenariosByFeatureName) {
        skippedScenariosByReportableName = new HashMap<>();
        if(skippedScenariosByFeatureName == null) {
            featureNames = Collections.emptySet();
            return;
        }
        List<PickleWrapper> pickles = skippedScenariosByFeatureName.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        final HashMap<String, List<PickleWrapper>> skippedScenariosByTag = createSkippedScenariosByTag(pickles);

        skippedScenariosByReportableName.putAll(skippedScenariosByFeatureName);
        featureNames = skippedScenariosByFeatureName.keySet();
        skippedScenariosByReportableName.putAll(skippedScenariosByTag);
    }

    private HashMap<String, List<PickleWrapper>> createSkippedScenariosByTag(final List<PickleWrapper> pickles) {
        final HashMap<String, List<PickleWrapper>> skippedScenariosByTag = new HashMap<>();
        pickles.forEach(pickleWrapper -> pickleWrapper.getPickle().getTags().forEach( tag ->
                        skippedScenariosByTag.compute(tag, (key, pickleWrappers) -> {
                            List<PickleWrapper> skippedPickles = new ArrayList<>();
                            if(pickleWrappers != null) {
                                skippedPickles = pickleWrappers;
                            }
                            skippedPickles.add(pickleWrapper);
                            return skippedPickles;
                        })
                                                                                    ));
        return skippedScenariosByTag;
    }

    public Set<String> getFeatureNames() {
        return featureNames;
    }

    public List<String> getScenarioNamesByReportableName(final String name) {
        return skippedScenariosByReportableName.getOrDefault(name, Collections.emptyList()).stream()
                .map(pickleWrapper -> pickleWrapper.getPickle().getName())
                .collect(Collectors.toList());
    }

    public List<Pickle> getScenariosByReportableName(final String name) {
        return skippedScenariosByReportableName.getOrDefault(name, Collections.emptyList()).stream()
                .map(PickleWrapper::getPickle)
                .collect(Collectors.toList());
    }

    public int getTotalScenarioCount() {
        // Casting to int because we should never have more than 2147483647 scenarios.
        // The tests would never finish.
        return (int) skippedScenariosByReportableName.values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .count();
    }

}
