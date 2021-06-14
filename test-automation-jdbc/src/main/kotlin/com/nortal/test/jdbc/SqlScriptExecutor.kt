package com.nortal.test.jdbc

import com.nortal.test.core.file.FileResolver
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class SqlScriptExecutor(
    private val jdbcTemplate: JdbcTemplate,
    private val fileResolver: FileResolver
) {
    companion object {
        private val PREFIX_CLASSPATH = "classpath:"
    }

    fun executeFromClasspath(filePath: String) {
        val sql = fileResolver.getFileAsString(PREFIX_CLASSPATH + filePath)

        jdbcTemplate.execute(sql)
    }

}