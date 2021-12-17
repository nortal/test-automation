package com.nortal.test.core.rest.interceptors

import com.atlassian.oai.validator.OpenApiInteractionValidator
import com.atlassian.oai.validator.model.Request
import com.atlassian.oai.validator.model.Response
import com.atlassian.oai.validator.report.LevelResolver
import com.atlassian.oai.validator.schema.SchemaValidator
import com.atlassian.oai.validator.report.ValidationReport
import java.io.IOException
import com.atlassian.oai.validator.model.SimpleResponse
import com.atlassian.oai.validator.model.SimpleRequest
import com.nortal.test.core.rest.error.SwaggerValidationException
import okhttp3.*
import okhttp3.internal.http.StatusLine
import okio.Buffer
import java.lang.NumberFormatException
import java.net.HttpURLConnection
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

/**
 * An Interceptor which validates API request/response pairs against the Swagger definition.
 *
 *
 * TODO: this is needs to be finished.
 */
class SwaggerRequestValidatorInterceptor(swaggerJsonUrlOrPayload: String?) : Interceptor {
    protected val validator: OpenApiInteractionValidator

    init {
        // additionalProperties validation is broken for schemas with allOf
        // Disable it. AssertLosslessJson should cover it.
        val levelResolver = LevelResolver.create()
            .withLevel(
                SchemaValidator.ADDITIONAL_PROPERTIES_KEY,
                ValidationReport.Level.IGNORE
            )
            .build()
        validator = OpenApiInteractionValidator
            .createFor(swaggerJsonUrlOrPayload!!)
            .withLevelResolver(levelResolver).build()
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.squareup.okhttp.Interceptor#intercept(com.squareup.okhttp.Interceptor
	 * .Chain)
	 */
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val okRequest = chain.request()
        val okResponse = chain.proceed(okRequest)
        val requestBody = okRequest.body()
        val requestType = requestBody?.contentType()
        val responseBody = okResponse.body()
        val responseType = responseBody?.contentType()
        if (requestType != null && !isJson(requestType)
            || responseType != null && !isJson(responseType)
        ) {
            // Don't validate non-JSON requests or responses since their
            // handling is not well specified in the OpenAPI spec and
            // swagger-request-validator doesn't support file type.
            return okResponse
        }
        val vRequest = okhttpToValidatorRequest(
            okRequest
        )
        val vResponse = okhttpToValidatorResponse(
            okResponse
        )
        val report = validator.validate(vRequest, vResponse)
        val messages = report.messages
        if (!messages.isEmpty()) {
            throw SwaggerValidationException(messages)
        }
        return okResponse
    }

    companion object {
        /**
         * Pattern to match an ISO 8601 datetime with microsecond (or higher) precision as a JSON string value.
         */
        protected val ISO_DATE_TIME_MICRO_JSON = Pattern
            .compile(
                "\"(?<date>[0-9]{4}-[0-9]{2}-[0-9]{2})"
                        + "T(?<time>[0-9]{2}:[0-9]{2}:[0-9]{2})"
                        + "\\.(?<msec>[0-9]{3})(?<unsec>[0-9]+)"
                        + "(?<tz>Z|(?:\\+|-)[0-9]{2}:[0-9]{2})\""
            )

        protected fun guessCharset(contentType: MediaType?): Charset? {
            if (contentType == null) {
                return StandardCharsets.UTF_8
            }
            var charset = contentType.charset()
            if (charset == null) {
                // HTTP 1.1 default text charset is ISO-8859-1
                charset = if ("text" == contentType.type()) StandardCharsets.ISO_8859_1 else StandardCharsets.UTF_8
            }
            return charset
        }

        protected fun guessCharset(requestBody: RequestBody): Charset? {
            return guessCharset(requestBody.contentType())
        }

        protected fun guessCharset(responseBody: ResponseBody?): Charset? {
            return guessCharset(responseBody!!.contentType())
        }

        /**
         * Converts a ResponseBody to String in a way that allows it to be re-read by later interceptors and the caller.
         *
         *
         * Code based on HttpLoggingInterceptor.
         *
         * @param responseBody Response body to read.
         * @return Response body as a String.
         * @throws IOException
         */
        @Throws(IOException::class)
        protected fun responseBodyToString(responseBody: ResponseBody?): String {
            val charset = guessCharset(responseBody)
            val source = responseBody!!.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            // Note: Content length may not be known until response is buffered
            if (responseBody.contentLength() == 0L) {
                return ""
            }
            source.buffer.clone().use { buffer -> return buffer.readString(charset!!) }
        }

        protected fun isJson(mediaType: MediaType): Boolean {
            val subtype = mediaType.subtype()
            return subtype == "json" || subtype == "x-json" || subtype.endsWith("+json")
        }

        protected fun responseBodyFixup(responseBody: String?): String {
            // Times with more than millisecond precision do not validate
            // https://github.com/daveclayton/json-schema-validator/issues/166
            val dateMatcher =
                ISO_DATE_TIME_MICRO_JSON.matcher(responseBody)
            return dateMatcher
                .replaceAll("\"\${date}T\${time}.\${msec}\${tz}\"")
        }

        @Throws(IOException::class)
        protected fun okhttpToValidatorResponse(
            response: okhttp3.Response
        ): Response {
            var responseBuilder = SimpleResponse.Builder(
                response.code()
            )

            // It's difficult to determine if the response body is actually empty.
            // It returns a ResponseBody with .contentLength() == -1.
            // Use same method as HttpLoggingInterceptor
            if (hasBody(response)) {
                val responseBody = response.body()
                var responseBodyString = responseBodyToString(responseBody)
                responseBodyString = responseBodyFixup(responseBodyString)
                responseBuilder = responseBuilder.withBody(responseBodyString)
            }
            return responseBuilder.build()
        }

        @Throws(IOException::class)
        protected fun okhttpToValidatorRequest(
            request: okhttp3.Request
        ): Request {
            val requestMethod = Request.Method.valueOf(request.method())
            val requestUrl = request.url()
            val requestPath = requestUrl.encodedPath()
            val requestBody = request.body()
            var requestBuilder = SimpleRequest.Builder(
                requestMethod, requestPath
            )
            if (requestBody != null) {
                val requestBodyString = requestBodyToString(requestBody)
                requestBuilder = requestBuilder.withBody(requestBodyString)
            }
            for (i in 0 until requestUrl.querySize()) {
                val name = requestUrl.queryParameterName(i)
                val value = requestUrl.queryParameterValue(i)
                requestBuilder = requestBuilder.withQueryParam(name, value)
            }
            return requestBuilder.build()
        }

        @Throws(IOException::class)
        protected fun requestBodyToString(requestBody: RequestBody): String {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            val charset = guessCharset(requestBody)
            return buffer.readString(charset!!)
        }

        /**
         * Returns true if the response must have a (possibly 0-length) body. See RFC 2616 section 4.3.
         */
        fun hasBody(response: okhttp3.Response): Boolean {
            // HEAD requests never yield a body regardless of the response headers.
            if (response.request().method() == "HEAD") {
                return false
            }
            val responseCode = response.code()
            if ((responseCode < StatusLine.HTTP_CONTINUE || responseCode >= 200)
                && responseCode != HttpURLConnection.HTTP_NO_CONTENT && responseCode != HttpURLConnection.HTTP_NOT_MODIFIED
            ) {
                return true
            }
            // If the Content-Length or Transfer-Encoding headers disagree with the
            // response code, the response is malformed. For best compatibility, we
            // honor the headers.
            return if (contentLength(response.headers()) != -1L || "chunked".equals(
                    response.header("Transfer-Encoding"),
                    ignoreCase = true
                )
            ) {
                true
            } else false
        }

        private fun contentLength(headers: Headers): Long {
            return stringToLong(headers["Content-Length"])
        }

        private fun stringToLong(s: String?): Long {
            return if (s == null) {
                -1
            } else try {
                s.toLong()
            } catch (e: NumberFormatException) {
                -1
            }
        }
    }
}