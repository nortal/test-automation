/**
 * Copyright (c) 2023 Nortal AS
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

import com.nortal.test.asserts.Assertion;
import com.nortal.test.asserts.ValidationHelper;
import com.nortal.test.asserts.ValidationService;
import com.nortal.test.demo.api.DemoApi;
import com.nortal.test.demo.api.controller.DemoController;
import io.cucumber.java.en.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

public class DemoDefs {
    @Autowired
    private DemoApi demoApi;
    @Autowired
    private ValidationService validationService;

    @Step("Demo status is retrieved")
    public void initPagePresent() {
        ResponseEntity<DemoController.DemoApiStatus> response = demoApi.getApiStatus();

        new ValidationHelper(validationService, response, "Verify response")
                .assertion(new Assertion.Builder()
                        .message("Verify status code")
                        .expression("statusCodeValue")
                        .expectedValue(200)
                        .build())
                .assertion(new Assertion.Builder()
                        .message("Verify status")
                        .expression("body.status")
                        .expectedValue("OK")
                        .build())
                .execute();
    }
}
