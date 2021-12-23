package com.nortal.test.feign.util

import okhttp3.Request
import okio.Buffer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

object FeignUtils {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun convertRequestBodyToString(request: Request): String? {
        if (request.body == null) {
            return null
        }
        try {
            Buffer().use { buffer ->
                val copy = request.newBuilder().build()
                if (copy.body != null) {
                    copy.body!!.writeTo(buffer)
                }
                return buffer.readUtf8()
            }
        } catch (e: IOException) {
            log.error("Failed to read body", e)
            return null
        }
    }
}