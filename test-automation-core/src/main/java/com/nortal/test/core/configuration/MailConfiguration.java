package com.nortal.test.core.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfiguration {

	@Value("${spring.mail.username:}")
	private String username;
	@Value("${spring.mail.password:}")
	private String password;
	@Value("${spring.mail.host}")
	private String host;
	@Value("${spring.mail.port}")
	private int port;
	@Value("${spring.mail.protocol}")
	private String protocol;

	@Bean
	public JavaMailSender javaMailSender(){
		final JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setProtocol(protocol);
		sender.setHost(host);
		sender.setPort(port);
		sender.setUsername(username);
		sender.setPassword(password);
		return sender;
	}


}
