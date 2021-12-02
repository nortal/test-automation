package com.nortal.test.core.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.nortal.test.core.rest.error.RestCallException
import okhttp3.Request
import okio.Buffer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

/**
 * Retrofit helper utilities.
 */
@Component
class RetrofitCallHelper(@Qualifier("apiObjectMapper") private val apiObjectMapper: ObjectMapper) {
    private val log = LoggerFactory.getLogger(RetrofitCallHelper::class.java)
//    private val GSON = GsonBuilder().create()


/*    fun <T> safelyExecute(apiCall: Call<T>): T {
        //TODO disable in future by default
        return request(apiCall, Function<Response<T>, T> { obj: Response<T>? -> getResponseBody() }, true)
    }*/

    fun <T> safelyExecute(apiCall: Call<T>): HttpResponse<T> {
        //TODO disable in future by default
        //throw RestCallException.newException(response)
        val response = executeRequest(apiCall, true)
        val httpResponse = getHttpResponse(response)
        if (httpResponse.isSuccessful) {
            return httpResponse
        }
        throw RestCallException.newException(response)
    }

    fun safelyExecuteBadHttpResponse(apiCall: Call<*>): HttpResponse<Unit> {
        return safelyExecuteBadHttpResponse(apiCall, Unit::class.java)
    }

    fun <ERROR_TYPE> safelyExecuteBadHttpResponse(apiCall: Call<*>, expectedResponseType: Class<ERROR_TYPE>): HttpResponse<ERROR_TYPE> {
        val response = executeRequest(apiCall, true)
        val httpResponse = getBadHttpResponse(response, expectedResponseType)
        if (!httpResponse.isSuccessful) {
            return httpResponse
        }
        throw RestCallException.newException(response)
    }


    fun convertRequestBodyToString(request: Request): String? {
        if (request.body() == null) {
            return null
        }
        try {
            Buffer().use { buffer ->
                val copy = request.newBuilder().build()
                if (copy.body() != null) {
                    copy.body()!!.writeTo(buffer)
                }
                return buffer.readUtf8()
            }
        } catch (e: IOException) {
            log.error("Failed to read body", e)
            return null
        }
    }

    private fun <T> getHttpResponse(response: Response<T>): HttpResponse<T> {
        return HttpResponse(
            isSuccessful = response.isSuccessful,
            statusCode = response.code(),
            body = response.body(),
            headers = response.headers().toMultimap()
        )
    }

    private fun <ERROR_TYPE> getBadHttpResponse(response: Response<*>, expectedResponseType: Class<ERROR_TYPE>): HttpResponse<ERROR_TYPE> {
        if (!response.isSuccessful) {
            val body: ERROR_TYPE? = when (expectedResponseType) {
                Unit::class.java -> null
                else -> getErrorBody(response, expectedResponseType)
            }

            return HttpResponse(
                isSuccessful = response.isSuccessful,
                statusCode = response.code(),
                body = body,
                headers = response.headers().toMultimap()
            )
        }
        throw RestCallException.newException(response)
    }

    private fun <T> getErrorBody(response: Response<*>, errorBodyType: Class<T>): T? {
        if (response.errorBody() != null) {
            val bodyStr = response.errorBody()!!.string()
            if (bodyStr.isNotBlank()) {

                if (String::class.java == errorBodyType) {
                    return bodyStr as T
                }

                return try {
                    apiObjectMapper.readValue(bodyStr, errorBodyType)
                } catch (e: IOException) {
                    throw RestCallException.newException(e)
                }
            }
        }
        return null
    }


    private fun <T> executeRequest(apiCall: Call<T>, addToCollection: Boolean): Response<T> {
        return try {

            apiCall.execute()
        } catch (e: IOException) {
            throw RestCallException.newException(e)
        } finally {

        }
    }

}