/**
 * Copyright (c) 2022 Nortal AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.nortal.test.core.report.model

/**
 * Convenience wrapper for headers
 */
class HttpHeaders private constructor(private val headers: Map<String, HttpHeader>) {

    companion object {

        fun fromMap(headerMap: Map<String, List<String>>): HttpHeaders {
            val headers = headerMap
                .mapKeys { entry -> entry.key.uppercase() }
                .mapValues { entry -> HttpHeader(entry.key.uppercase(), entry.value) }
            return HttpHeaders(headers)
        }
    }


    fun asMap(): Map<String, HttpHeader> {
        return headers
    }

    fun getByName(name: String): HttpHeader? {
        return headers[name.uppercase()]
    }

    fun getFirstByName(name: String): String? {
        return getByName(name)?.getFirstValue()
    }
}

data class HttpHeader(
    val name: String, val values: List<String>
) {

    fun getFirstValue(): String {
        return values.first()
    }
}