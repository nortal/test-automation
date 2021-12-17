package com.nortal.test.core.services.report.html

import lombok.Builder
import lombok.Data

@Data
@Builder
class PostmanDataReport {
    private val enabled = false
    private val collections: List<CollectionData>? = null
    private val environments: List<String>? = null

    @Data
    @Builder
    class CollectionData {
        private val id: String? = null
        private val name: String? = null
        private val fileName: String? = null
    }
}