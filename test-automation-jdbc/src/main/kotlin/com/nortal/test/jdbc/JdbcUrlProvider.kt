package com.nortal.test.jdbc

interface JdbcUrlProvider {
    fun getJdbcUrl(): String

    fun getInternalJdbcUrl(): String
}