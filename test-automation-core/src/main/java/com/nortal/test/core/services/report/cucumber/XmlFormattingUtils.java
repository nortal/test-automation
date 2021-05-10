package com.nortal.test.core.services.report.cucumber;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class XmlFormattingUtils {

	private final String INDENT_PROPERTY = "{https://xml.apache.org/xslt}indent-amount";
	private final String INDENT_AMOUNT = "1";
	private final String YES = "yes";


	public String prettyPrintXml(String input) {
		final Source xmlInput = new StreamSource(new StringReader(input));
		final StringWriter stringWriter = new StringWriter();
		final TransformerFactory transformerFactory = TransformerFactory.newInstance();

		try {
			final Transformer transformer = transformerFactory.newTransformer();

			transformer.setOutputProperty(OutputKeys.INDENT, YES);
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, YES);
			transformer.setOutputProperty(INDENT_PROPERTY, INDENT_AMOUNT);
			transformer.transform(xmlInput, new StreamResult(stringWriter));
		} catch (Exception e) {
			log.error("Failed to transform string to XML {}", input);
		}

		return stringWriter.toString().trim();
	}
}
