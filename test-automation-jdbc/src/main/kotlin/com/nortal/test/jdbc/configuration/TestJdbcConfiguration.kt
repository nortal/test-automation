package com.nortal.test.jdbc.configuration

import com.nortal.test.jdbc.JdbcDataSourceProperties
import com.nortal.test.jdbc.JdbcUrlProvider
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@Configuration
@EnableConfigurationProperties(JdbcDataSourceProperties::class)
@ComponentScan("com.nortal.test.jdbc")
open class TestJdbcConfiguration {

    @Bean
    @Primary
    open fun primaryDataSource(
        dataSourceProperties: JdbcDataSourceProperties,
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
    open fun jdbcTemplate(dataSource: DataSource): JdbcTemplate {
        return JdbcTemplate(dataSource)
    }
}