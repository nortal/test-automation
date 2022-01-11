package com.nortal.test.aws

import com.nortal.test.aws.configuration.AwsConfiguration
import com.nortal.test.aws.service.S3FileStore
import com.nortal.test.core.configuration.TestConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.nio.file.Path

@ActiveProfiles("test")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = [TestConfiguration::class, AwsConfiguration::class],
)
class S3FileStoreIntTest {
    @Autowired
    private lateinit var s3FileStore: S3FileStore

    @Test
    fun shouldUploadFolder() {

        s3FileStore.uploadDir(Path.of("build/resources/test/test-data/"), "target-dir")
    }
}