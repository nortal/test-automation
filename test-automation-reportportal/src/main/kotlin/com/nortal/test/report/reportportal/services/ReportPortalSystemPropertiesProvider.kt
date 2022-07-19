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
package com.nortal.test.report.reportportal.services

import com.nortal.test.core.cucumber.SystemPropertiesProvider
import com.epam.reportportal.utils.properties.ListenerProperty.API_KEY
import com.epam.reportportal.utils.properties.ListenerProperty.BASE_URL
import com.epam.reportportal.utils.properties.ListenerProperty.BATCH_SIZE_LOGS
import com.epam.reportportal.utils.properties.ListenerProperty.PROJECT_NAME
import com.epam.reportportal.utils.properties.ListenerProperty.LAUNCH_NAME

class ReportPortalSystemPropertiesProvider: SystemPropertiesProvider {

    private val mandatoryProperties = mapOf(
            "report." + BASE_URL.propertyName to BASE_URL.propertyName,
            "report." + PROJECT_NAME.propertyName to PROJECT_NAME.propertyName,
            "report." + LAUNCH_NAME.propertyName to LAUNCH_NAME.propertyName,
            "report." + API_KEY.propertyName to API_KEY.propertyName,
            "report." + BATCH_SIZE_LOGS.propertyName to BATCH_SIZE_LOGS.propertyName
    )

    override fun getProperties(): Map<String, String> {
        return mandatoryProperties
    }
}