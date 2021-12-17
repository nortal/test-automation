package com.nortal.test.core.rest.error

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.nortal.test.core.exceptions.TestExecutionException
import retrofit2.Response
import java.io.IOException
import java.lang.Exception

class RestCallException : TestExecutionException {
    var code = 0
        private set

    private constructor(message: String) : super(message) {}
    private constructor(exception: Exception) : super(exception) {}

    companion object {
        private val GSON = GsonBuilder().setPrettyPrinting().create()
        fun newException(exception: Exception): RestCallException {
            return RestCallException(exception)
        }

        fun newException(response: Response<*>): RestCallException {
            var errorResponse = getRawErrorResponse(response)
            errorResponse = prettifyErrorResponse(errorResponse)
            val request = response.raw().request()
            val url = request.url().toString()
            val code = response.code()
            val message = """Rest call ${request.method()} $url
has failed with status: $code
Body: $errorResponse"""
            val restCallException = RestCallException(message)
            restCallException.code = code
            return restCallException
        }

        private fun prettifyErrorResponse(errorResponse: String): String {
            return try {
                """
     
     ${GSON.toJson(GSON.fromJson(errorResponse, Any::class.java))}
     """.trimIndent()
            } catch (e: JsonSyntaxException) {
                errorResponse
            }
        }

        private fun getRawErrorResponse(response: Response<*>): String {
            val errorBody = response.errorBody() ?: return ""
            return try {
                errorBody.string()
            } catch (e: IOException) {
                ""
            }
        }
    }
}