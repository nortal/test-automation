package com.nortal.test.core.logs;

import java.io.ByteArrayOutputStream;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;

public class TestExecutionLogAppender extends OutputStreamAppender<ILoggingEvent> {
	private static final ByteArrayOutputStream OUTPUT_STREAM = new ByteArrayOutputStream();

	public TestExecutionLogAppender() {
		this.setOutputStream(OUTPUT_STREAM);
	}

	/**
	 * Get whole log as string.
	 */
	public static String getLogs() {
		return OUTPUT_STREAM.toString();
	}

}
