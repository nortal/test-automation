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
package com.nortal.test.core.services.hooks

import com.nortal.test.core.services.CucumberScenarioProvider

/**
 * Interface for performing after scenario cleanup. Any bean that implements this interface will have its cleanup method called during cucumber @After
 * tag.
 */
interface AfterScenarioHook {
    /**
     * Perform after scenario operations.
     */
    fun after(scenario: CucumberScenarioProvider?)

    /**
     * Returns order in which the hooks will be run. Lower order runs first.
     * Default order: 10000
     *
     * @return order
     */
    fun afterScenarioOrder(): Int {
        return DEFAULT_ORDER
    }

    companion object {
        const val DEFAULT_ORDER = 10000
    }
}