package com.nortal.test.core.services.report.cucumber

import com.nortal.test.core.testng.AbstractTestNGIntegrationTests
import lombok.experimental.UtilityClass
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.StringReader
import java.io.StringWriter
import java.lang.Exception
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

@UtilityClass
@Slf4j
class XmlFormattingUtils {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    private val INDENT_PROPERTY = "{https://xml.apache.org/xslt}indent-amount"
    private val INDENT_AMOUNT = "1"
    private val YES = "yes"

    fun prettyPrintXml(input: String?): String {
        val xmlInput: Source = StreamSource(StringReader(input))
        val stringWriter = StringWriter()
        val transformerFactory = TransformerFactory.newInstance()
        try {
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, YES)
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, YES)
            transformer.setOutputProperty(INDENT_PROPERTY, INDENT_AMOUNT)
            transformer.transform(xmlInput, StreamResult(stringWriter))
        } catch (e: Exception) {
            log.error("Failed to transform string to XML {}", input)
        }
        return stringWriter.toString().trim { it <= ' ' }
    }
}