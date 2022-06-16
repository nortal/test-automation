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