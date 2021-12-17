package com.nortal.test.core.testng

import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

/**
 * This class is responsible for preparing the workspace for a test suite run.
 */
class WorkspaceManager {
    /**
     * Prepares the workspace by removing all json files from the test output directory.
     */
    fun prepareWorkspace() {
        LOGGER.info("Clearing scenario results json files from output directory: {}", TEST_OUTPUT_DIR)
        jsonsFromTestOutput.forEach { path: Path -> delete(path) }
    }

    private fun delete(path: Path) {
        try {
            LOGGER.info("Deleting report json {}", path)
            Files.deleteIfExists(path)
        } catch (e: IOException) {
            LOGGER.error("Could not delete json due to IO exception", e)
        }
    }

    /**
     * Cleans up the workspace by removing any jsons that are 0kb in size, so that report generator would not crash.
     */
    fun cleanupWorkspace() {
        LOGGER.info("Clearing test output directory from 0b sized json files.")
        jsonsFromTestOutput.forEach { json: Path -> deleteIfEmpty(json) }
    }

    private fun deleteIfEmpty(json: Path) {
        try {
            val size = Files.size(json)
            if (size == 0L) {
                LOGGER.warn("Deleting {} json", json)
                Files.delete(json)
            }
        } catch (e: IOException) {
            LOGGER.error("Could not perform 0b sized json cleanup due to IO exception", e)
        }
    }

    private val jsonsFromTestOutput: Stream<Path>
        private get() = try {
            Files.walk(Paths.get(TEST_OUTPUT_DIR))
                .filter { it: Path -> it.toFile().path.endsWith(".json") }
        } catch (e: IOException) {
            LOGGER.info("Test output directory does not exist: {}", TEST_OUTPUT_DIR)
            Stream.empty()
        }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(WorkspaceManager::class.java)
        private const val TEST_OUTPUT_DIR = "build/test-output"
    }
}