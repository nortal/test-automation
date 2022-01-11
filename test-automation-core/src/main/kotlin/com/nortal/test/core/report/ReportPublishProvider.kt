package com.nortal.test.core.report

import java.nio.file.Path

interface ReportPublishProvider {

    fun publish(resultDir: Path, reportName: String, entryFileName: String)
}