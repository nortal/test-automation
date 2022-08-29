/**
 * Copyright (c) 2022 Nortal AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.nortal.test.core.services

import io.cucumber.spring.ScenarioScope
import org.junit.jupiter.api.Assertions
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.util.*

/**
 * Scenario context. Data is scoped to a particular scenario and is not shared between test suite.
 */
@Component
@ScenarioScope
data class ScenarioContext(
    /**
     * Unique identifier
     */
    val scenarioId: String = UUID.randomUUID().toString(),
    /**
     * Scenario start time
     */
    val scenarioStartTime: OffsetDateTime = OffsetDateTime.now(),
    /**
     * Hashmap based step data context. Useful for sharing simple variables between steps.
     */
    val stepData: MutableMap<String, Any> = HashMap()
) {
    /**
     * Get step data from key - value mapping.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getStepData(key: String): T? {
        return stepData[key] as? T?
    }

    /**
     * Get step data from key - value mapping that should be present during this call.
     *
     * @throws org.opentest4j.AssertionFailedError if value is null
     */
    fun <T> getRequiredStepData(key: String): T? {
        val data = getStepData(key) as T?
        Assertions.assertNotNull(data)

        return data
    }

    /**
     * Puts a value in step data map.
     */
    fun putStepData(key: String, value: Any) {
        stepData[key] = value;
    }
}