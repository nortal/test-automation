package com.nortal.test

import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.Suite

@Suite
@IncludeEngines("cucumber")
@Suppress("UtilityClassWithPublicConstructor", "UnnecessaryAbstractClass")
abstract class TestRunner