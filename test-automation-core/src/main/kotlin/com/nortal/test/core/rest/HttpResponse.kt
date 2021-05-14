package com.nortal.test.core.rest

import retrofit2.Response

data class HttpResponse<T>(
    val isSuccessful: Boolean,
    val statusCode: Int,
    val body: T?,
    val headers: Map<String, List<String>> = mapOf()
)