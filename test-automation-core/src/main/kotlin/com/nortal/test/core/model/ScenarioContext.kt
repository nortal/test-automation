package com.nortal.test.core.model

import java.time.OffsetDateTime
import java.util.*


class ScenarioContext {
    private val scenarioId = UUID.randomUUID().toString()
    private val scenarioStartTime = OffsetDateTime.now()
    private val common = ScenarioCommonContext()


    class ScenarioCommonContext {
//        private val lastHttpResponse: HttpResponse<*>? = null
        private val headers: MutableMap<String, String> = HashMap()
        fun addHeader(key: String, value: String) {
            headers[key] = value
        }
    }
}