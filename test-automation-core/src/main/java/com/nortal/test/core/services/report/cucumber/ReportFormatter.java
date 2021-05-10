package com.nortal.test.core.services.report.cucumber;

import com.nortal.test.core.services.ScenarioContainer;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class is responsible for attaching an html section to the report.
 *
 * <p>The section is attached and has a name a title and several inner sections (either collapsible or not)
 */
@Component
public class ReportFormatter implements InitializingBean {

	private static final String MAIN_TEMPLATE = "template.html";

	private final Map<String, String> templates = new HashMap<>();

	private final ScenarioContainer scenarioContainer;

	public ReportFormatter(@Lazy final ScenarioContainer scenarioContainer) {
		this.scenarioContainer = scenarioContainer;
	}

	/**
	 * Embeds the provided section to the html report as a collapsible section.
	 *
	 * @param attachment to embed
	 */
	public void formatAndAddToReport(final Attachment attachment) {
		final String attachmentBody = attachment.getSections()
				.stream()
				.map(this::applySectionTemplate)
				.collect(Collectors.joining("\n"));

		final byte[] bytes = getTemplate(MAIN_TEMPLATE)
				.replace("{{TITLE}}", attachment.getTitle())
				.replace("{{CONTENT}}", attachmentBody)
				.getBytes();
		scenarioContainer.getScenario().embed(bytes, "text/html", attachment.getName());
	}

	@Override
	public void afterPropertiesSet() {
		loadTemplate(MAIN_TEMPLATE);
		Arrays.stream(SectionType.values()).map(SectionType::getFileName).forEach(this::loadTemplate);
	}

	private String getTemplate(final String name) {
		return templates.get(name);
	}

	private void loadTemplate(final String name) {
		final URL resource = Thread.currentThread().getContextClassLoader().getResource("report/" + name);
		try (InputStream inputStream = Objects.requireNonNull(resource).openStream()) {
			templates.put(name, new String(inputStream.readAllBytes()));
		} catch (Exception e) {
			throw new AssertionError("Could not load template for reporting", e);
		}
	}

	private String applySectionTemplate(final Triple<String, SectionType, String> section) {
		return String.format(getTemplate(section.getMiddle().getFileName()), section.getLeft(), section.getRight());
	}

	public static class Attachment {
		private String name = "Attachment"; //appears on the collapsible link
		private String title = ""; //appears on the top when expanded
		private final List<Triple<String, SectionType, String>> sections = new ArrayList<>();

		public static Attachment create() {
			return new Attachment();
		}

		/**
		 * Appears on the collapsible link.
		 *
		 * @param name string
		 * @return this
		 */
		public Attachment setName(final String name) {
			this.name = name;
			return this;
		}

		/**
		 * Appears on the top when expanded.
		 *
		 * @param title string
		 * @return this
		 */
		public Attachment setTitle(final String title) {
			this.title = title;
			return this;
		}

		public Attachment addSection(final String name, final SectionType type, final String content) {
			sections.add(Triple.of(name, type, content));
			return this;
		}

		public String getName() {
			return name;
		}

		public String getTitle() {
			return title;
		}

		public List<Triple<String, SectionType, String>> getSections() {
			return sections;
		}
	}

	public enum SectionType {
		STANDARD("details_template.html"),
		TABLE("table_template.html"),
		COLLAPSIBLE("details_collapsible_template.html"),
		BARE("details_bare_template.html");

		private String fileName;

		SectionType(final String fileName) {
			this.fileName = fileName;
		}

		public String getFileName() {
			return fileName;
		}
	}
}
