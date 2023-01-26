package com.nortal.test.core.report

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.module.SimpleSerializers
import org.springframework.core.io.Resource

class ResourceSerializingModule : SimpleModule() {

    override fun setupModule(context: SetupContext) {
        val serializers = SimpleSerializers()
        serializers.addSerializer(Resource::class.java, ResourceSerializer())
        context.addSerializers(serializers)
    }

}

class ResourceSerializer : JsonSerializer<Resource>() {
    override fun serialize(value: Resource, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("filename", value.filename)
        gen.writeStringField("contentLength", value.contentLength().toString())
        gen.writeEndObject()
    }

}