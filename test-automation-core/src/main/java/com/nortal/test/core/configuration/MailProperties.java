package com.nortal.test.core.configuration;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Setter
@ConfigurationProperties(prefix = "report.mail")
public class MailProperties {
	private boolean enabled;
	private String from;
	private String to;

	/**
	 * Returns an array of email report recipient emails.
	 * @return array of emails
	 */
	public String[] getTo() {
		if (to == null) {
			throw new IllegalStateException("Attempting to send email report without having report.mail.to property set!");
		}

		return Arrays.stream(to.contains(",") ? to.split(",") : to.split("\\s")).map(String::trim).toArray(String[]::new);
	}

	/**
	 * Mail address from which the test report email is sent.
	 * @return email address
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * Whether sending report email is endabled
	 * @return true if enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}
}
