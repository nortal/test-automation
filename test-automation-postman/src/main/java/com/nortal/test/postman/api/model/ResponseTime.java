package com.nortal.test.postman.api.model;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

/**
 * When the cookie expires.
 * <p>
 * The time taken by the request to complete. If a number, the unit is milliseconds. If the response is manually created, this can be set to `null`.
 */
@Data
@JsonDeserialize(using = ResponseTime.Deserializer.class)
@JsonSerialize(using = ResponseTime.Serializer.class)
public class ResponseTime {
	private Double doubleValue;
	private String stringValue;

	static class Deserializer extends JsonDeserializer<ResponseTime> {
		@Override
		public ResponseTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
				throws IOException, JsonProcessingException {
			ResponseTime value = new ResponseTime();
			switch (jsonParser.currentToken()) {
			case VALUE_NULL:
				break;
			case VALUE_NUMBER_INT:
			case VALUE_NUMBER_FLOAT:
				value.doubleValue = jsonParser.readValueAs(Double.class);
				break;
			case VALUE_STRING:
				String string = jsonParser.readValueAs(String.class);
				value.stringValue = string;
				break;
			default:
				throw new IOException("Cannot deserialize ResponseTime");
			}
			return value;
		}
	}

	static class Serializer extends JsonSerializer<ResponseTime> {
		@Override
		public void serialize(ResponseTime obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
			if (obj.doubleValue != null) {
				jsonGenerator.writeObject(obj.doubleValue);
				return;
			}
			if (obj.stringValue != null) {
				jsonGenerator.writeObject(obj.stringValue);
				return;
			}
			jsonGenerator.writeNull();
		}
	}
}
