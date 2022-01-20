package com.nortal.test.core.cucumber

/**
 * Provided cucumber plugin name that will be used as an report generator.  Note: currently single report provider per instance is supported.
 */
interface TestReportProvider {

    /**
     * Provide cucumber plugin name that is usually set to cucumber.plugin property. It's a class name.
     */
    fun getCucumberPlugin(): String
}