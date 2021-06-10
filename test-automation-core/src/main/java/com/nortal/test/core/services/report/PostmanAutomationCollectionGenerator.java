package com.nortal.test.core.services.report;

import static com.nortal.test.postman.generate.PostmanEnvironmentGenerator.ENV_PREFIX;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.nortal.test.postman.model.PostmanExecutionContext;
import com.nortal.test.postman.util.PostmanCollectionFileNameUtils;
import com.nortal.test.postman.util.ReportFileUtils;
import com.nortal.test.postman.generate.PostmanArchiveGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.nortal.test.core.configuration.ReportProperties;
import com.nortal.test.core.model.TargetEnvironment;
import com.nortal.test.core.services.report.html.PostmanDataReport;
import com.nortal.test.postman.generate.PostmanCollectionGenerator;
import com.nortal.test.postman.generate.PostmanEnvironmentGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostmanAutomationCollectionGenerator {
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final ReportProperties reportProperties;
	private final PostmanCollectionGenerator postmanCollectionGenerator;
	private final PostmanEnvironmentGenerator postmanEnvironmentGenerator;
	private final PostmanArchiveGenerator postmanArchiveGenerator;

	@Value("${postman-generator.enabled:false}")
	private boolean enabled;

	private PostmanExecutionContext postmanExecutionContext;

	public void generate() {
		if (enabled) {
			final StopWatch stopWatch = StopWatch.createStarted();
			final PostmanExecutionContext context = getContext(true);

			postmanCollectionGenerator.generate(context);
			log.info("Postman collections have been generated in {} ms", stopWatch.getTime());
			postmanEnvironmentGenerator.generate(context);
			log.info("Postman collection environments have been generated in {} ms", stopWatch.getTime());
			postmanArchiveGenerator.generate(context);
			log.info("Postman collection archive has been generated in {} ms", stopWatch.getTime());
		} else {
			log.info("Postman collection generation is disabled.");
		}
	}

	public PostmanDataReport getDataReport() {
		final PostmanExecutionContext context = getContext();
		final List<PostmanDataReport.CollectionData> collections = createCollectionData(context);

		return PostmanDataReport.builder()
				.enabled(enabled)
				.collections(collections)
				.environments(context.getEnvironments())
				.build();
	}

	private List<PostmanDataReport.CollectionData> createCollectionData(final PostmanExecutionContext context) {
		if (!enabled) {
			return Collections.emptyList();
		}
		final String collectionDir = String.format(PostmanCollectionGenerator.COLLECTION_PATH, context.getOutputDir());
		return ReportFileUtils.listFileNames(collectionDir).stream()
				.filter(fileName -> !fileName.startsWith(ENV_PREFIX))
				.sorted()
				.map(fileName -> createCollectionData(fileName, context))
				.collect(Collectors.toList());
	}

	private PostmanDataReport.CollectionData createCollectionData(final String fileName, final PostmanExecutionContext context) {
		final String id = PostmanCollectionFileNameUtils.getFeatureId(fileName);
		final String name = context.getCollectionNames().get(id);
		return PostmanDataReport.CollectionData.builder()
				.id(id)
				.name(name)
				.fileName(fileName)
				.build();
	}

	private PostmanExecutionContext getContext() {
		return getContext(false);
	}

	private PostmanExecutionContext getContext(final boolean create) {
		if (postmanExecutionContext == null || create) {
			postmanExecutionContext = createContext();
		}
		return postmanExecutionContext;
	}

	private PostmanExecutionContext createContext() {
		final List<String> environmentProfiles = Arrays.stream(TargetEnvironment.values())
				.map(Enum::name)
				.map(String::toLowerCase)
				.collect(Collectors.toList());

		return PostmanExecutionContext.builder()
				.executionTitle(getExecutionTitle())
				.outputDir(reportProperties.getOutput())
				.environments(environmentProfiles)
				.collectionNames(new HashMap<>())
				.build();
	}

	private String getExecutionTitle() {
		final String name = StringUtils.defaultString(reportProperties.getBuildName(), "test-automation");

		return name + " " + DATE_TIME_FORMATTER.format(ZonedDateTime.now());
	}
}
