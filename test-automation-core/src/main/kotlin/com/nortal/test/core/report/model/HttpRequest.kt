package com.nortal.test.core.report.model

data class HttpRequest(
    val method: String,
    val url: String,
    val headers: HttpHeaders,
    val body: String?,
)