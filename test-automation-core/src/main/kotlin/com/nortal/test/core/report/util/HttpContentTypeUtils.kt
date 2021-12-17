package com.nortal.test.core.report.util

import org.springframework.http.MediaType

object HttpContentTypeUtils {
    fun isXmlContentType(contentType: String?): Boolean {
        return contentType?.contains(MediaType.APPLICATION_XML_VALUE) ?: false
    }

    fun isJsonContentType(contentType: String?): Boolean {
        return contentType?.contains(MediaType.APPLICATION_JSON_VALUE) ?: false
    }
}