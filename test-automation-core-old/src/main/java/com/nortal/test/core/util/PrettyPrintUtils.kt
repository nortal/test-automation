package com.nortal.test.core.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException

/**
 * Utils for pretty printing objects as json
 */
object PrettyPrintUtils {
    private val GSON = GsonBuilder().setPrettyPrinting().create()
    fun prettyPrint(`object`: Any): String {
        return try {
            """
     
     ${GSON.toJson(`object`, Any::class.java)}
     """.trimIndent()
        } catch (e: JsonSyntaxException) {
            `object`.toString()
        }
    }
}