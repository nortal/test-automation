package com.nortal.test.dev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@EnableZuulProxy
@SpringBootApplication
public class LocalDevProxyApplication {
	public static void main(String[] args) {
		SpringApplication.run(LocalDevProxyApplication.class, args);
	}
}