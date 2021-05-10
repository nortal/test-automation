package com.nortal.test.postman.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EnvironmentConfigurationReader {
	private static final String YAML_CONFIG_PATH_PATTERN = "classpath:application-%s.yaml";
	private static final ObjectMapper YAML_OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

	private final ClassPathReader classPathReader;

	public List<EnvironmentHostConfiguration> readConfigurationFiles(final List<String> environments) {
		return environments.stream()
				.map(this::readConfiguration)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	private Optional<EnvironmentHostConfiguration> readConfiguration(final String environment) {
		try {
			final Resource resource = readResource(environment);
			return Optional.of(new EnvironmentHostConfiguration(environment, findHostConfiguration(resource)));
		} catch (Exception fileNotFoundException) {
			return Optional.empty();
		}
	}

	Resource readResource(final String environment) {
		return classPathReader.readResource(String.format(YAML_CONFIG_PATH_PATTERN, environment));
	}

	Map<String, String> findHostConfiguration(final Resource resource) throws IOException {
		final JsonNode jsonNode = getProperties(resource).at("/integration/hosts");

		return YAML_OBJECT_MAPPER.convertValue(jsonNode, new TypeReference<>() {});
	}

	JsonNode getProperties(final Resource resource) throws IOException {
		return YAML_OBJECT_MAPPER.readValue(resource.getInputStream(), JsonNode.class);
	}

	@Getter
	@RequiredArgsConstructor
	public static class EnvironmentHostConfiguration {
		private final String environment;
		private final Map<String, String> hosts;
	}
}
