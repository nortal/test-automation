package com.nortal.test.core.services.report.html

import lombok.Data

@Data
class ChangedFilesReport {
    private val commits: List<Commit>? = null
    private val sinceDays: Long = 0
    fun size(): Int {
        return commits!!.size
    }

    val stats: String
        get() = String.format("There were %s commits to golden data during last %s days.\n", size(), sinceDays)
}