package com.nortal.test.core.model

import com.nortal.test.core.rest.HttpResponse
import lombok.Data
import lombok.Getter
import lombok.ToString
import java.time.OffsetDateTime
import java.util.*

@Getter
@ToString
class ScenarioContext {
    private val scenarioId = UUID.randomUUID().toString()
    private val scenarioStartTime = OffsetDateTime.now()
    private val common = ScenarioCommonContext()

    @Data
    class ScenarioCommonContext {
        private val lastHttpResponse: HttpResponse<*>? = null
        private val headers: MutableMap<String, String> = HashMap()
        fun addHeader(key: String, value: String) {
            headers[key] = value
        }
    }
}