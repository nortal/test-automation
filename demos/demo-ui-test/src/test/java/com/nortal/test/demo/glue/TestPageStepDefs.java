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
package com.nortal.test.demo.glue;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.nortal.test.demo.mediator.UiMediator;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

public class TestPageStepDefs {
	private static final String PAGE_HEADER_XPATH = "//h1[normalize-space()='Test Pages For Automating']";
	@Autowired
	private UiMediator uiMediator;

	@Given("Test Pages For Automating is open in browser")
	public void openPageInBrowser() {
		Selenide.open("https://testpages.herokuapp.com/");
		uiMediator.elementXpath(PAGE_HEADER_XPATH, true, 0L).shouldBe(Condition.visible);
	}

	@SneakyThrows
	@When("User clicks on {} link")
	public void clickOnLink(String linkName) {
		Thread.sleep(500L);
		uiMediator.elementXpath(getLinkByName(linkName), true, 0L).click();
	}

	@Then("Several paragraphs are visible")
	public void validateParagraphPresence(DataTable dataTable) {
		dataTable.asList().forEach(entry ->
				uiMediator.elementXpath(getParagraphByTextXpath(entry), true, 0L).shouldBe(Condition.visible));
	}

	private String getParagraphByTextXpath(String text) {
		return "//p[text()='" + text + "']";
	}

	private String getLinkByName(String name) {
		return "//a[text()='" + name + "']";
	}
}
