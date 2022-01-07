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