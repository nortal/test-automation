package com.nortal.test.core.cucumber

interface TestReportProvider {

    /**
     * Provide cucumber plugin name that is usually set to cucumber.plugin property. It's a class name.
     */
    fun getCucumberPlugin(): String
}