package com.nortal.test.core.services.report.cucumber;

import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportHtmlTableGenerator {

	public String generateTable(final List<List<String>> table, final boolean appendRowNums) {
		final StringBuilder html = new StringBuilder("<table class=\"table table-striped table-fit-content\">");

		populateHeader(html, table.get(0), appendRowNums);
		populateBody(html, table, appendRowNums);

		html.append("</table>");
		return html.toString();
	}

	private void populateHeader(final StringBuilder html, final List<String> headerValues, final boolean appendRowNums) {
		final List<String> finalHeaderValues;
		if (appendRowNums) {
			finalHeaderValues = new ImmutableList.Builder<String>().addAll(headerValues).add("#").build();
		} else {
			finalHeaderValues = headerValues;
		}

		html.append("<thead><tr>");

		finalHeaderValues.forEach(value -> html.append("<th>").append(value).append("</th>"));
		html.append("</tr></thead>");
	}

	//TODO optimize, quick hack
	private void populateBody(final StringBuilder html, final List<List<String>> table, final boolean appendRowNums) {
		html.append("<tbody>");
		for (int i = 1; i < table.size(); i++) {
			final List<String> rowValues = table.get(i);

			String rowCss = "";
			if (rowValues.stream().anyMatch("OK"::equals)) {
				rowCss = "success";
			} else if (rowValues.stream().anyMatch("FAILED"::equals)) {
				rowCss = "danger";
			} else if (rowValues.stream().anyMatch("SKIPPED"::equals)) {
				rowCss = "warning";
			}
			html.append("<tr class=\"").append(rowCss).append("\">");

			if (appendRowNums) {
				html.append("<td>").append(i).append("</td>");
			}
			rowValues.forEach(value -> html.append("<td>").append(value).append("</td>"));
			html.append("</tr>");
		}
		html.append("</tbody>");
	}
}
