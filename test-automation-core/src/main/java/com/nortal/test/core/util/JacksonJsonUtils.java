package com.nortal.test.core.util;

import java.io.IOException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Jackson backed JSON serialization utils.
 */
public final class JacksonJsonUtils {
	private static final ObjectMapper MAPPER = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

	/**
	 * Deserialize json string.
	 *
	 * @param value string deserialize
	 * @param clazz target class
	 * @param <T>   generic type of target clas
	 * @return Deserialized object
	 * @throws IOException on failure
	 */
	public static <T> T deserialize(final String value, final Class<T> clazz) throws IOException {
		return MAPPER.readValue(value, clazz);
	}

	/**
	 * Deserialize json string.
	 *
	 * @param value        string deserialize
	 * @param valueTypeRef target class
	 * @param <T>          generic type of target clas
	 * @return Deserialized object
	 * @throws IOException on failure
	 */
	@SuppressWarnings("rawtypes")
	public static <T> T deserialize(final String value, final TypeReference<T> valueTypeRef) throws IOException {
		return MAPPER.readValue(value, valueTypeRef);
	}

	/**
	 * Marshals EIP response wrapper to json.
	 *
	 * @param objectToSerialize value to serialize
	 * @return JSON string
	 * @throws IOException on failure
	 */
	public static String serialize(final Object objectToSerialize) throws IOException {
		return MAPPER.writeValueAsString(objectToSerialize);
	}

	/**
	 * Default constructor.
	 */
	private JacksonJsonUtils() {
		super();
	}
}
