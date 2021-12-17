package com.nortal.test.core.file

import lombok.extern.slf4j.Slf4j
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.stereotype.Component
import org.springframework.util.FileCopyUtils
import java.io.IOException
import java.io.InputStreamReader
import java.io.UncheckedIOException
import java.nio.charset.StandardCharsets

@Slf4j
@Component
class FileResolver(private val resourceLoader: ResourceLoader) {
    fun getFileAsString(filepath: String): String {
        return asString(getResource(filepath))
    }

    private fun asString(resource: Resource): String {
        try {
            InputStreamReader(resource.inputStream, StandardCharsets.UTF_8).use { reader -> return FileCopyUtils.copyToString(reader) }
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    private fun getResource(filepath: String): Resource {
        return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResource(filepath)
    }
}