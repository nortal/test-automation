package com.nortal.test.core.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import java.io.IOException

/**
 * Jackson backed JSON serialization utils.
 */
object JacksonJsonUtils {
    private val MAPPER = ObjectMapper()
        .registerModule(JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

    /**
     * Deserialize json string.
     *
     * @param value string deserialize
     * @param clazz target class
     * @param <T>   generic type of target clas
     * @return Deserialized object
     * @throws IOException on failure
    </T> */
    @Throws(IOException::class)
    fun <T> deserialize(value: String?, clazz: Class<T>?): T {
        return MAPPER.readValue(value, clazz)
    }

    /**
     * Deserialize json string.
     *
     * @param value        string deserialize
     * @param valueTypeRef target class
     * @param <T>          generic type of target clas
     * @return Deserialized object
     * @throws IOException on failure
    </T> */
    @Throws(IOException::class)
    fun <T> deserialize(value: String?, valueTypeRef: TypeReference<T>?): T {
        return MAPPER.readValue(value, valueTypeRef)
    }

    /**
     * Marshals EIP response wrapper to json.
     *
     * @param objectToSerialize value to serialize
     * @return JSON string
     * @throws IOException on failure
     */
    @Throws(IOException::class)
    fun serialize(objectToSerialize: Any?): String {
        return MAPPER.writeValueAsString(objectToSerialize)
    }
}