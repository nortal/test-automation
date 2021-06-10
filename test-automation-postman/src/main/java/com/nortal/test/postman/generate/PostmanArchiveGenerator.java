package com.nortal.test.postman.generate;

import static com.nortal.test.postman.generate.PostmanCollectionGenerator.COLLECTION_PATH;
import static com.nortal.test.postman.generate.PostmanEnvironmentGenerator.ENV_PREFIX;

import java.util.function.Function;

import com.nortal.test.postman.model.PostmanExecutionContext;
import com.nortal.test.postman.util.ReportFileUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PostmanArchiveGenerator {
	public static final String ARCHIVE_PATH = "%s/cucumber-html-reports/collections.zip";

	@SneakyThrows
	public void generate(final PostmanExecutionContext context) {
		final String collectionDir = String.format(COLLECTION_PATH, context.getOutputDir());
		final String archivePath = String.format(ARCHIVE_PATH, context.getOutputDir());
		ReportFileUtils.zipDirectory(collectionDir, archivePath, getEntryPrefixResolver());
	}

	private Function<String, String> getEntryPrefixResolver() {
		return fileName -> fileName.startsWith(ENV_PREFIX) ? "env-config" : "collections";
	}
}

