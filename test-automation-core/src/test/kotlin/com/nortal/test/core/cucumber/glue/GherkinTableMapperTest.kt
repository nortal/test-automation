package com.nortal.test.core.cucumber.glue

import org.junit.jupiter.api.Test
import org.testng.Assert

internal class GherkinTableMapperTest {

    val map = mapOf<String, String?>(
        "status" to "[n/a]"
    )

    @Test
    fun `should transform na value`() {
        GherkinTableMapper.map(map) {
            Assert.assertNull(it.getString("status"))
        }
    }
}
