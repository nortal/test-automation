package com.nortal.test.postman.util;

import java.nio.file.Path;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostmanJsonUtils {
	private static final ObjectMapper PRETTY_OBJECT_MAPPER = new ObjectMapper()
			.enable(SerializationFeature.INDENT_OUTPUT)
			.setSerializationInclusion(JsonInclude.Include.NON_NULL);

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
			.setSerializationInclusion(JsonInclude.Include.NON_NULL);

	@SneakyThrows
	public static String serialize(final Object obj) {
		return PRETTY_OBJECT_MAPPER.writeValueAsString(obj);
	}

	@SneakyThrows
	public static void serializeToFile(final String fileName, final String path, final Object obj) {
		final Path outputPath = Path.of(path + fileName);
		OBJECT_MAPPER.writeValue(outputPath.toFile(), obj);
	}

	@SneakyThrows
	public static <T> T deserialize(final String json, final Class<T> valueType) {
		return OBJECT_MAPPER.readValue(json, valueType);
	}

	@SneakyThrows
	public static String prettifyJson(final String json) {
		Object jsonObj = PRETTY_OBJECT_MAPPER.readValue(json, Object.class);

		return serialize(jsonObj);
	}
}
