package com.nortal.test.core.rest.error;

import java.util.List;
import com.atlassian.oai.validator.report.ValidationReport.Message;

/**
 * Exception representing a swagger validation failure.
 */
public class SwaggerValidationException extends ValidationException {
	private static final long serialVersionUID = 924828565647941758L;

	protected static String messagesToString(List<Message> messages) {
		StringBuilder builder = new StringBuilder("Validation failed:");
		for (Message message : messages) {
			builder.append('\n');
			builder.append(message.toString());
		}
		return builder.toString();
	}

	protected final List<Message> messages;

	public SwaggerValidationException(List<Message> messages) {
		super(messagesToString(messages));
		this.messages = messages;
	}

	public List<Message> getMessages() {
		return messages;
	}
}