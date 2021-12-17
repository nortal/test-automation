package com.nortal.test.core.report.model

data class HttpResponse(
    val statusCode: Int,
    val headers: HttpHeaders,
    val body: String?,
)
