/**
 * Copyright (c) 2022 Nortal AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.nortal.test.demo.mediator;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.nortal.test.core.services.ScenarioExecutionContext;
import com.nortal.test.core.util.RetryingInvoker;
import com.nortal.test.demo.configuration.TestDemoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UiMediator {
	private final AtomicInteger screenshotCounter = new AtomicInteger();
	private final TestDemoProperties testDemoProperties;
	private final ScenarioExecutionContext scenarioExecutionContext;

	public enum ElementType {XPATH, ID, CLASS_NAME, NAME, TAG_NAME, CSS}

	private void demoMode(String element, ElementType type) {
		final String script = resolveDemoScript(element, type);
		log.trace("Executing javascript snippet: {}", script);

		Optional.ofNullable(script)
				.ifPresent(scr -> RetryingInvoker.retry(() -> Selenide.executeJavaScript(scr)));

	}

	private String resolveDemoScript(String element, ElementType type) {
		final String styleAttribute = "setAttribute('style', 'border:4px dashed #ffdd99')";

		switch (type) {
			case XPATH:
				return "var query = document.evaluate(\"" + element + "\", document, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);"
						+ "  var results = Array(query.snapshotLength).fill(0).map((element, index) =>  query.snapshotItem(index));"
						+ "   for (let element of results) { element." + styleAttribute + "}";
			case ID:
				return "document.getElementById(\"" + element + "\")." + styleAttribute + ";";
			case CLASS_NAME:
				return "for (let element of document.getElementsByClassName(\"" + element + "\")) { element." + styleAttribute + " };";
			case NAME:
				return "for (let element of document.getElementsByName(\"" + element + "\")) { element." + styleAttribute + " };";
			case TAG_NAME:
				return "for (let element of document.getElementsByTagName(\"" + element + "\")) { element." + styleAttribute + " };";
			case CSS:
				return "for (let elementByCss of document.querySelectorAll(\"" + element + "\")) { elementByCss." + styleAttribute + " };";
		}
		return null;
	}

	private void takeScreenshot() {
		byte[] data = Selenide.screenshot(OutputType.BYTES);
		String name = "screenshot-" + screenshotCounter.incrementAndGet();
		scenarioExecutionContext.getScenario().attach(data, "image/png", name);
	}

	private void lazyExecution() {
		Selenide.sleep(testDemoProperties.getLazyExecutionTime() * 1000);
	}

	public SelenideElement elementXpath(String xpath, boolean highlightOverride, Long hardWaitBefore) {
		Selenide.sleep(hardWaitBefore);
		if (testDemoProperties.isLazyExecution()) {
			lazyExecution();
		}

		if (testDemoProperties.isDemoMode() && highlightOverride) {
			demoMode(xpath, ElementType.XPATH);
		}

		SelenideElement action = RetryingInvoker.retry(() -> Selenide.element(By.xpath(xpath)));
		if (testDemoProperties.isScreenshotsSteps()) {
			takeScreenshot();
		}

		return action;
	}
}
