/**
 * Copyright (c) 2022 Nortal AS
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.nortal.test.core.cucumber.glue;

import com.nortal.test.core.services.hooks.AfterSuiteHook;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static java.util.Comparator.comparingInt;

/**
 * Triggering {@link  AfterSuiteHook} beans before spring context is closed.
 * Note: we can't do the same for {@link  com.nortal.test.core.services.hooks.BeforeSuiteHook} due to the way cucumber initializes it.
 */
public class AfterSuiteHookGlue {
    private static final Logger log = LoggerFactory.getLogger(AfterSuiteHookGlue.class);

    private static ApplicationContext context;

    @Autowired
    public AfterSuiteHookGlue(final ApplicationContext appContext) {
        context = appContext;
    }

    @Before
    public void before() {
        //dummy, just to trigger initialization.
    }

    @AfterAll
    public static void afterSuite() {
        if (context != null) {
            context.getBeansOfType(AfterSuiteHook.class).values().stream()
                    .sorted(comparingInt(AfterSuiteHook::afterSuitOrder))
                    .forEach(afterSuiteHook -> {
                        log.info("Running after suite hook for: {}", afterSuiteHook.getClass().getName());
                        afterSuiteHook.afterSuite();
                    });
        } else {
            log.warn("Spring Context was not initialized! Skipping after suite hooks");
        }
    }

}
