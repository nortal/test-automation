package com.nortal.test.postman.api.model;

import java.io.IOException;
import java.util.List;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonDeserialize(using = Src.Deserializer.class)
@JsonSerialize(using = Src.Serializer.class)
public class Src {
	private List<Object> anythingArrayValue;
	private String stringValue;

	static class Deserializer extends JsonDeserializer<Src> {
		@Override
		public Src deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
			Src value = new Src();
			switch (jsonParser.currentToken()) {
			case VALUE_NULL:
				break;
			case VALUE_STRING:
				String string = jsonParser.readValueAs(String.class);
				value.stringValue = string;
				break;
			case START_ARRAY:
				value.anythingArrayValue = jsonParser.readValueAs(new TypeReference<List<Object>>() {});
				break;
			default:
				throw new IOException("Cannot deserialize Src");
			}
			return value;
		}
	}

	static class Serializer extends JsonSerializer<Src> {
		@Override
		public void serialize(Src obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
			if (obj.anythingArrayValue != null) {
				jsonGenerator.writeObject(obj.anythingArrayValue);
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
