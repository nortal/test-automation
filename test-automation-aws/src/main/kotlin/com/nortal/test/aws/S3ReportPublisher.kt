package com.nortal.test.aws

import com.amazonaws.services.s3.AmazonS3
import com.nortal.test.aws.configuration.AwsProperties
import com.nortal.test.aws.service.S3FileStore
import com.nortal.test.core.report.ReportPublishProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class S3ReportPublisher(
    private val awsProperties: AwsProperties,
    private val s3FileStore: S3FileStore,
    private val amazonS3: AmazonS3
) : ReportPublishProvider {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun publish(resultDir: Path, reportName: String, entryFileName: String) {
        if (awsProperties.s3PublishReport) {
            s3FileStore.uploadDir(resultDir, reportName)


            val indexUrl = amazonS3.getUrl(awsProperties.s3BucketName, "$reportName/index.html")
            log.info("Report is available at {}", indexUrl)
        }
    }
}