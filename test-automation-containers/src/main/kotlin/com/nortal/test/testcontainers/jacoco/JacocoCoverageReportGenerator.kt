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
package com.nortal.test.testcontainers.jacoco

import com.nortal.test.core.exception.TestAutomationException
import com.nortal.test.core.services.TestableApplicationInfoProvider
import com.nortal.test.core.services.hooks.AfterSuiteHook
import com.nortal.test.testcontainers.configuration.TestableContainerJacocoProperties
import org.apache.commons.lang3.time.StopWatch
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.CoverageBuilder
import org.jacoco.core.analysis.IBundleCoverage
import org.jacoco.core.data.ExecutionDataWriter
import org.jacoco.core.runtime.RemoteControlReader
import org.jacoco.core.runtime.RemoteControlWriter
import org.jacoco.core.tools.ExecFileLoader
import org.jacoco.report.DirectorySourceFileLocator
import org.jacoco.report.FileMultiReportOutput
import org.jacoco.report.IReportVisitor
import org.jacoco.report.html.HTMLFormatter
import org.jacoco.report.xml.XMLFormatter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.TimeUnit

@Component
open class JacocoCoverageReportGenerator(
    private val jacocoProperties: TestableContainerJacocoProperties,
    private val testableApplicationInfoProvider: TestableApplicationInfoProvider
) :
    AfterSuiteHook {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val FILE_EXEC = "system-tests.exec"
        private const val FILE_XML = "system-tests.xml"
    }

    override fun afterSuite() {
        if (!jacocoProperties.enabled) {
            log.info("Jacoco is disabled. Skipping report generation.")
            return
        }

        try {
            val stopWatch = StopWatch.createStarted()
            log.info("Generating Jacoco report...")
            transferExecutionData()
            generateReport()
            log.info("Jacoco report generated in {}ms.", stopWatch.getTime(TimeUnit.MILLISECONDS))
        } catch (e: Exception) {
            log.error("Failed to generate Jacoco report", e)
            throw TestAutomationException("System tests coverage report generator failed", e)
        }
    }

    @Throws(IOException::class)
    private fun generateReport() {
        val execFileLoader = ExecFileLoader()

        Files.newInputStream(getFileOnDestDirPath(FILE_EXEC)).use {
            execFileLoader.load(it)
        }

        val bundleCoverage = analyzeStructure(execFileLoader)
        createXmlReport(bundleCoverage, execFileLoader)
        createHtmlReport(bundleCoverage, execFileLoader)
    }

    @Throws(IOException::class)
    private fun analyzeStructure(execFileLoader: ExecFileLoader): IBundleCoverage {
        val coverageBuilder = CoverageBuilder()
        val analyzer = Analyzer(execFileLoader.executionDataStore, coverageBuilder)

        Files.find(
            Paths.get(".."), 10,
            { path: Path, _: BasicFileAttributes? ->
                path.toString().matches(Regex(jacocoProperties.structureAnalysisRegex))
            })
            .use { stream ->
                stream.forEach { path ->
                    try {
                        analyzer.analyzeAll(path.toFile())
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        return coverageBuilder.getBundle("System tests coverage report")
    }

    @Throws(IOException::class)
    private fun createXmlReport(bundleCoverage: IBundleCoverage, execFileLoader: ExecFileLoader) {
        Files.newOutputStream(getFileOnDestDirPath(FILE_XML)).use {
            val visitor = XMLFormatter().createVisitor(it)
            visitResults(bundleCoverage, execFileLoader, visitor)
        }
    }

    @Throws(IOException::class)
    private fun createHtmlReport(bundleCoverage: IBundleCoverage, execFileLoader: ExecFileLoader) {
        val htmlFormatter = HTMLFormatter()
        val visitor = htmlFormatter
            .createVisitor(FileMultiReportOutput(File(jacocoProperties.destReportDir)))
        visitResults(bundleCoverage, execFileLoader, visitor)
    }

    @Throws(IOException::class)
    private fun visitResults(bundleCoverage: IBundleCoverage, execFileLoader: ExecFileLoader, visitor: IReportVisitor) {
        visitor.visitInfo(
            execFileLoader.sessionInfoStore.infos,
            execFileLoader.executionDataStore.contents
        )
        Files.find(
            Paths.get(".."), 10,
            { path: Path, _: BasicFileAttributes? ->
                path.toString().matches(Regex(jacocoProperties.sourceCodeLookupRegex))
            })
            .use { stream ->
                stream.forEach { path ->
                    try {
                        // Populate the report structure with the bundle coverage information.
                        // Call visitGroup if you need groups in your report.
                        visitor.visitBundle(
                            bundleCoverage,
                            DirectorySourceFileLocator(path.toFile(), "UTF-8", 2)
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        visitor.visitEnd()
    }

    @Throws(IOException::class)
    private fun transferExecutionData() {
        createTargetDir()
        val mappedPort = testableApplicationInfoProvider.getMappedPort(jacocoProperties.port)
        Socket(InetAddress.getByName(jacocoProperties.host), mappedPort).use { socket ->
            Files.newOutputStream(
                getFileOnDestDirPath(FILE_EXEC)
            ).use { localFile ->
                val localWriter = ExecutionDataWriter(localFile)
                val writer = RemoteControlWriter(socket.getOutputStream())
                val reader = RemoteControlReader(socket.getInputStream())
                reader.setSessionInfoVisitor(localWriter)
                reader.setExecutionDataVisitor(localWriter)

                // Send a dump command and read the response:
                writer.visitDumpCommand(true, false)
                if (!reader.read()) {
                    throw IOException("Socket closed unexpectedly.")
                }
            }
        }
    }

    private fun createTargetDir() {
        val targetDir = File(jacocoProperties.destDir)
        if (!targetDir.exists()) {
            targetDir.mkdir()
        }
    }

    private fun getFileOnDestDirPath(fileName: String): Path {
        return Paths.get(jacocoProperties.destDir + fileName)
    }
}