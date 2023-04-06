/**
 * Copyright (c) 2022 Nortal AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.nortal.test.report.allure.plugin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
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

    companion object {
        const val LOG_FILE = "test-automation-exec.log"
    }

    @Throws(IOException::class)
    override fun aggregate(
        configuration: Configuration,
        launchesResults: List<LaunchResults>,
        outputDirectory: Path
    ) {
        val dataFolder = Files.createDirectories(outputDirectory.resolve("data"))

        //move logs
        Files.move(
            outputDirectory.parent.resolve(LOG_FILE),
            dataFolder.resolve("test-automation-exec.log")
        )
        //create mapping file
        val dataFile = dataFolder.resolve("logs.json")
        Files.newOutputStream(dataFile).use { os -> objectMapper.writeValue(os, getData()) }
    }

    private fun getData(): List<LogDto> {
        return listOf(
            LogDto(
                name = "test framework",
                filename = LOG_FILE
            )
        )
    }

    data class LogDto(
        val name: String,
        val filename: String
    )

}