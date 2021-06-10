package com.nortal.test.core.services.report;

import com.nortal.test.core.services.report.cucumber.JsonFormattingUtils;
import com.nortal.test.core.services.report.cucumber.ReportFormatter;
import com.nortal.test.core.services.report.cucumber.ReportHtmlTableGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Service responsible for exposing methods that attach information to the test report.
 */
@Component
@RequiredArgsConstructor
public class TestReportService {

	private final ReportFormatter reportFormatter;
	private final ReportHtmlTableGenerator reportHtmlTableGenerator;

	/**
	 * Attaches the provided object to the report, formatting it as json.
	 *
	 * @param title   of the section
	 * @param content to be attached
	 */
	public void attachJson(final String title, final Object content) {
		final ReportFormatter.Attachment attachment = ReportFormatter.Attachment.create()
				.setName(title)
				.addSection("", ReportFormatter.SectionType.BARE, JsonFormattingUtils.prettyPrintHtmlJson(content));
		reportFormatter.formatAndAddToReport(attachment);
	}

	/**
	 * Attaches the provided text to the report.
	 *
	 * @param title of the section
	 * @param text  to be attached
	 */
	public void attachText(final String title, final String text) {
		final ReportFormatter.Attachment attachment = ReportFormatter.Attachment.create()
				.setName(title)
				.addSection("", ReportFormatter.SectionType.BARE, text);
		reportFormatter.formatAndAddToReport(attachment);
	}

	/**
	 * Attaches the provided object to the report, formatting it as json.
	 *
	 * @param title   of the section
	 * @param content to be attached
	 */
	public void attachTable(final String title, final List<List<String>> content) {
		final ReportFormatter.Attachment attachment = ReportFormatter.Attachment.create()
				.setName(title)
				.addSection("", ReportFormatter.SectionType.TABLE, reportHtmlTableGenerator.generateTable(content, true));
		reportFormatter.formatAndAddToReport(attachment);
	}

	/**
	 * Attaches served mocks.
	 *
	 * @param title      title
	 * @param id         id
	 * @param request    request
	 * @param servedMock servedMock
	 * @param response   response
	 */
	public void attachServedMocks(final String title, final String id, final String request, final String servedMock, final String response) {
		final ReportFormatter.Attachment attachment = ReportFormatter.Attachment.create()
				.setName(title)
				.setTitle("Mock Id: " + id)
				.addSection("Request matcher:", ReportFormatter.SectionType.COLLAPSIBLE, servedMock)
				.addSection("Actual request:", ReportFormatter.SectionType.COLLAPSIBLE, request)
				.addSection("Actual response:", ReportFormatter.SectionType.COLLAPSIBLE, response);
		reportFormatter.formatAndAddToReport(attachment);
	}

	/**
	 * Attaches multiple jsons in the same attachment.
	 * Separate collapsible blocks will have map keys as their titles and values as content.
	 *
	 * @param jsonMap map of jsons to attach
	 */
	public void attachJson(final String attachmentTitle, final Map<String, Object> jsonMap) {
		final ReportFormatter.Attachment attachment = ReportFormatter.Attachment.create()
				.setName(attachmentTitle)
				.setTitle(attachmentTitle);
		jsonMap.forEach(
				(key, val) -> attachment.addSection(key, ReportFormatter.SectionType.COLLAPSIBLE, JsonFormattingUtils.prettyPrintHtmlJson(val)));
		reportFormatter.formatAndAddToReport(attachment);
	}
}
