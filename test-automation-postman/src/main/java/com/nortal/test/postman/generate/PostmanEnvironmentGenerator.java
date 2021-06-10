package com.nortal.test.postman.generate;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.nortal.test.postman.api.model.PostmanEnvironmentValue;
import com.nortal.test.postman.model.PostmanExecutionContext;
import com.nortal.test.postman.util.EnvironmentConfigurationReader;
import com.nortal.test.postman.util.PostmanJsonUtils;
import com.nortal.test.postman.util.ReportFileUtils;
import org.springframework.stereotype.Component;
import com.nortal.test.postman.api.model.PostmanEnvironment;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PostmanEnvironmentGenerator {
	private static final String POSTMAN_VAR_SCOPE = "environment";
	public static final String ENV_PREFIX = "postman-env";
	private static final String FILENAME_PATTERN = ENV_PREFIX + "-%s.json";

	private final EnvironmentConfigurationReader environmentConfigurationReader;

	public void generate(final PostmanExecutionContext context) {

		environmentConfigurationReader.readConfigurationFiles(context.getEnvironments())
				.forEach(configuration -> generateEnvVariables(context, configuration));
	}

	private void generateEnvVariables(final PostmanExecutionContext context,
	                                  final EnvironmentConfigurationReader.EnvironmentHostConfiguration configuration) {
		final PostmanEnvironment.PostmanEnvironmentBuilder postmanEnvironmentBuilder = PostmanEnvironment.builder()
				.id(UUID.randomUUID().toString())
				.name("Automation-ENV: " + configuration.getEnvironment())
				.variableScope(POSTMAN_VAR_SCOPE)
				.exportedUsing("dcp-test-automation")
				.exportedAt(ZonedDateTime.now().toString());

		configuration.getHosts().forEach((key, value) -> postmanEnvironmentBuilder.value(createValue(key, value)));

		final String outputDir = String.format(PostmanCollectionGenerator.COLLECTION_PATH, context.getOutputDir());
		ReportFileUtils.createDirectory(outputDir);

		final String fileName = String.format(FILENAME_PATTERN, configuration.getEnvironment());
		PostmanJsonUtils.serializeToFile(fileName, outputDir, postmanEnvironmentBuilder.build());
	}

	private PostmanEnvironmentValue createValue(final String key, final String value) {
		return PostmanEnvironmentValue.builder()
				.enabled(true)
				.key(key)
				.value(value)
				.build();
	}
}
