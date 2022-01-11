package com.nortal.test.core.report

import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
open class ReportPublisher(
    private val reportNameProvider: ReportNameProvider,
    private val reportPublishProviders: List<ReportPublishProvider>
) {

    open fun publish(reportDir: Path, entryFileName: String) {
        reportPublishProviders.forEach { it.publish(reportDir, reportNameProvider.getName(), entryFileName) }
    }
}