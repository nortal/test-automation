package com.nortal.test.jdbc

import com.nortal.test.core.file.ClasspathFileResolver
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class SqlScriptExecutor(
    private val jdbcTemplate: JdbcTemplate,
    private val classpathFileResolver: ClasspathFileResolver
) {
    companion object {
        private val PREFIX_CLASSPATH = "classpath:"
    }

    fun executeFromClasspath(filePath: String) {
        val sql = classpathFileResolver.getFileAsString(PREFIX_CLASSPATH + filePath)

        jdbcTemplate.execute(sql)
    }

}