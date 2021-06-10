package com.nortal.test.core.toggling;

public interface FeatureToggleAware {

    boolean isFeatureEnabled(final String featurePath);

}
