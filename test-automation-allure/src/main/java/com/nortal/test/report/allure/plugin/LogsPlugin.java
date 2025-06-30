/*
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
package com.nortal.test.report.allure.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import io.qameta.allure.Aggregator2;
import io.qameta.allure.ReportStorage;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.LaunchResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * The plugin adds Log tab to the report.
 */
public class LogsPlugin implements Aggregator2 {
    private static final Logger log = LoggerFactory.getLogger(LogsPlugin.class);

    public static final String LOG_FILE = "test-automation-exec.log";
    public static final String CONTAINER_LOGS = "container-logs";
    public static final String DATA_DIR = "data/";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new KotlinModule.Builder().build())
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

    @Override
    public void aggregate(
            Configuration configuration,
            List<LaunchResults> launchesResults,
            ReportStorage storage
    ) {
        try {
            processMainLogFile(storage);
            processContainerLogs(storage);
            createMappingFile(storage);
        } catch (Exception e) {
            log.error("Failed to aggregate logs", e);
        }
    }

    private void processMainLogFile(ReportStorage storage) {
        String logDirProperty = System.getProperty("testExecLogDir");
        if (logDirProperty == null || logDirProperty.isEmpty()) {
            log.warn("testExecLogDir property is not set, skipping main log file processing");
            return;
        }

        Path logFile = Path.of(logDirProperty, LOG_FILE);
        if (Files.exists(logFile)) {
            addFileToStorage(storage, DATA_DIR + LOG_FILE, logFile);
        } else {
            log.warn("Main log file not found at {}", logFile);
        }
    }

    private void processContainerLogs(ReportStorage storage) throws IOException {
        String logDirProperty = System.getProperty("testExecLogDir");
        if (logDirProperty == null || logDirProperty.isEmpty()) {
            log.warn("testExecLogDir property is not set, skipping container logs processing");
            return;
        }

        Path containerLogsDir = Path.of(logDirProperty, CONTAINER_LOGS);
        if (!Files.isDirectory(containerLogsDir)) {
            log.info("Container logs directory not found at {}", containerLogsDir);
            return;
        }

        try (var paths = Files.walk(containerLogsDir)) {
            paths.filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            Path relativePath = containerLogsDir.relativize(file);
                            String targetPath = DATA_DIR + CONTAINER_LOGS + "/" + relativePath;
                            addFileToStorage(storage, targetPath, file);
                            Files.deleteIfExists(file);
                        } catch (IOException e) {
                            log.error("Failed to process container log file: {}", file, e);
                        }
                    });
        }
    }

    private void addFileToStorage(ReportStorage storage, String targetPath, Path sourceFile) {
        storage.addDataFile(targetPath, sourceFile);
        log.debug("Added file to storage: {}", targetPath);
    }

    private void createMappingFile(ReportStorage storage) throws IOException {
        var tempFile = File.createTempFile("ta-log-" + UUID.randomUUID(), ".json").toPath();
        try (var os = Files.newOutputStream(tempFile)) {
            objectMapper.writeValue(os, getData());
        }
        storage.addDataFile(DATA_DIR + "logs.json", tempFile);
        log.debug("Created mapping file: logs.json");
    }

    private List<LogDto> getData() {
        return List.of(new LogDto("test framework", LOG_FILE));
    }

    public record LogDto(String name, String filename) {
    }
}