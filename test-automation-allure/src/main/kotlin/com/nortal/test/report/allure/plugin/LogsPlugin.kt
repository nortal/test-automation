package com.nortal.test.report.allure.plugin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.nortal.test.core.logs.TestExecutionLogAppender
import io.qameta.allure.Aggregator
import io.qameta.allure.core.Configuration
import io.qameta.allure.core.LaunchResults
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * The plugin adds Log tab to the report.
 *
 */
@Suppress("unused")
class LogsPlugin : Aggregator {
    private val objectMapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())
        .registerModule(JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(SerializationFeature.INDENT_OUTPUT, true)

    @Throws(IOException::class)
    override fun aggregate(
        configuration: Configuration,
        launchesResults: List<LaunchResults>,
        outputDirectory: Path
    ) {
        val dataFolder = Files.createDirectories(outputDirectory.resolve("data"))
        val dataFile = dataFolder.resolve("logs.json")

        Files.newOutputStream(dataFile).use { os -> objectMapper.writeValue(os, getData()) }
    }

    private fun getData(): List<LogDto> {
        return listOf(
            LogDto(
                name = "test framework",
                content = TestExecutionLogAppender.getLogs()
            )
        )
    }

    data class LogDto(
        val name: String,
        val content: String
    )

}