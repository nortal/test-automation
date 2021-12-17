package com.nortal.test.core.model

import java.util.*

class QueryCache {
    private val queryToIdentifier: MutableMap<String, String> = HashMap()
    fun add(query: String, resolvedIdentifier: String) {
        queryToIdentifier[query] = resolvedIdentifier
    }

    operator fun get(query: String): Optional<String> {
        return Optional.ofNullable(queryToIdentifier[query])
    }
}