package com.nortal.test.core.services.report.html;

import lombok.Data;

@Data
public class ReportErrorMessage {
	private final String error;
	private final String description;
	private final Long count;
}
