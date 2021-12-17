package com.nortal.test.core.services.report.html

import lombok.Data

@Data
class ReportErrorMessage {
    private val error: String? = null
    private val description: String? = null
    private val count: Long? = null
}