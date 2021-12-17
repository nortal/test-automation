package com.nortal.test.core.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
class MailConfiguration {
    @Value("\${spring.mail.username:}")
    private val username: String? = null

    @Value("\${spring.mail.password:}")
    private val password: String? = null

    @Value("\${spring.mail.host}")
    private val host: String? = null

    @Value("\${spring.mail.port}")
    private val port = 0

    @Value("\${spring.mail.protocol}")
    private val protocol: String? = null
    @Bean
    fun javaMailSender(): JavaMailSender {
        val sender = JavaMailSenderImpl()
        sender.protocol = protocol
        sender.host = host
        sender.port = port
        sender.username = username
        sender.password = password
        return sender
    }
}