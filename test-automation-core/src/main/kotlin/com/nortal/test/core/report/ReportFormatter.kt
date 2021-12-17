package com.nortal.test.core.report

import com.nortal.test.core.services.ScenarioExecutionContext
import io.cucumber.java.Scenario
import org.apache.commons.lang3.tuple.Triple
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import java.util.*
import java.util.stream.Collectors

/**
 * This class is responsible for attaching an html section to the report.
 *
 *
 * The section is attached and has a name a title and several inner sections (either collapsible or not)
 */
@Component
class ReportFormatter(
    private val scenarioExecutionContext: ScenarioExecutionContext
) : InitializingBean {
    private val templates: MutableMap<String, String> = HashMap()

    companion object {
        private const val MAIN_TEMPLATE = "template.html"
    }

    fun formatAndAddToReport(attachment: Attachment) {
        formatAndAddToReport(attachment, scenarioExecutionContext.getScenario())
    }

    /**
     * Embeds the provided section to the html report as a collapsible section.
     *
     * @param attachment to embed
     */
    fun formatAndAddToReport(attachment: Attachment, scenario: Scenario) {
        val attachmentBody = attachment.getSections()
            .stream()
            .map { section: Triple<String, SectionType, String> -> applySectionTemplate(section) }
            .collect(Collectors.joining("\n"))
        val bytes = getTemplate(MAIN_TEMPLATE)
            ?.replace("{{TITLE}}", attachment.title)
            ?.replace("{{CONTENT}}", attachmentBody)
            ?.toByteArray()

        scenario.attach(bytes, "text/html", attachment.name)
    }

    override fun afterPropertiesSet() {
        loadTemplate(MAIN_TEMPLATE)
        Arrays.stream(SectionType.values()).map { obj: SectionType -> obj.fileName }
            .forEach { name: String -> loadTemplate(name) }
    }

    private fun getTemplate(name: String): String? {
        return templates[name]
    }

    private fun loadTemplate(name: String) {
        val resource = Thread.currentThread().contextClassLoader.getResource("report/$name")
        try {
            Objects.requireNonNull(resource).openStream().use { inputStream -> templates.put(name, String(inputStream.readAllBytes())) }
        } catch (e: Exception) {
            throw AssertionError("Could not load template for reporting", e)
        }
    }

    private fun applySectionTemplate(section: Triple<String, SectionType, String>): String {
        return String.format(getTemplate(section.middle.fileName)!!, section.left, section.right)
    }

    class Attachment {
        var name = "Attachment" //appears on the collapsible link
            private set
        var title = "" //appears on the top when expanded
            private set
        private val sections: MutableList<Triple<String, SectionType, String>> = ArrayList()

        /**
         * Appears on the collapsible link.
         *
         * @param name string
         * @return this
         */
        fun setName(name: String): Attachment {
            this.name = name
            return this
        }

        /**
         * Appears on the top when expanded.
         *
         * @param title string
         * @return this
         */
        fun setTitle(title: String): Attachment {
            this.title = title
            return this
        }

        fun addSection(name: String, type: SectionType, content: String): Attachment {
            sections.add(Triple.of(name, type, content))
            return this
        }

        fun getSections(): List<Triple<String, SectionType, String>> {
            return sections
        }

        companion object {
            @JvmStatic
            fun create(): Attachment {
                return Attachment()
            }
        }
    }

    enum class SectionType(val fileName: String) {
        STANDARD("details_template.html"),
        TABLE("table_template.html"),
        COLLAPSIBLE("details_collapsible_template.html"),
        BARE("details_bare_template.html");

    }


}