package com.nortal.test.core.testng;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for preparing the workspace for a test suite run.
 */
public class WorkspaceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceManager.class);

    private static final String TEST_OUTPUT_DIR = "build/test-output";

    /**
     * Prepares the workspace by removing all json files from the test output directory.
     */
    public void prepareWorkspace() {
        LOGGER.info("Clearing scenario results json files from output directory: {}", TEST_OUTPUT_DIR);
        getJsonsFromTestOutput().forEach(this::delete);
    }

    private void delete(Path path) {
        try {
            LOGGER.info("Deleting report json {}", path);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LOGGER.error("Could not delete json due to IO exception", e);
        }
    }

    /**
     * Cleans up the workspace by removing any jsons that are 0kb in size, so that report generator would not crash.
     */
    public void cleanupWorkspace() {
        LOGGER.info("Clearing test output directory from 0b sized json files.");
        getJsonsFromTestOutput().forEach(this::deleteIfEmpty);
    }

    private void deleteIfEmpty(Path json) {
        try {
            long size = Files.size(json);
            if (size == 0L) {
                LOGGER.warn("Deleting {} json", json);
                Files.delete(json);
            }
        } catch (IOException e) {
            LOGGER.error("Could not perform 0b sized json cleanup due to IO exception", e);
        }
    }

    private Stream<Path> getJsonsFromTestOutput() {
        try {
            return Files.walk(Paths.get(TEST_OUTPUT_DIR))
                        .filter(it -> it.toFile().getPath().endsWith(".json"));
        } catch (IOException e) {
            LOGGER.info("Test output directory does not exist: {}", TEST_OUTPUT_DIR);
            return Stream.empty();
        }
    }
}
