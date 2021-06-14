package com.nortal.test.jdbc.configuration

import com.nortal.test.jdbc.JdbcUrlProvider
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.jdbc.JdbcProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@Configuration
class TestJdbcConfiguration {

    @Bean
    fun dataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    fun jdbcProperties(): JdbcProperties {
        return JdbcProperties()
    }

    @Bean
    @Primary
    fun primaryDataSource(
        dataSourceProperties: DataSourceProperties,
        jdbcUrlProvider: JdbcUrlProvider
    ): DataSource {
        return DataSourceBuilder.create()
            .driverClassName(dataSourceProperties.driverClassName)
            .url(jdbcUrlProvider.getJdbcUrl())
            .username(dataSourceProperties.username)
            .password(dataSourceProperties.password)
            .build()
    }

    @Bean
    @Primary
    fun jdbcTemplate(dataSource: DataSource, properties: JdbcProperties): JdbcTemplate {
        val jdbcTemplate = JdbcTemplate(dataSource)
        val template = properties.template
        jdbcTemplate.fetchSize = template.fetchSize
        jdbcTemplate.maxRows = template.maxRows
        if (template.queryTimeout != null) {
            jdbcTemplate.queryTimeout = template.queryTimeout.seconds.toInt()
        }
        return jdbcTemplate
    }
}