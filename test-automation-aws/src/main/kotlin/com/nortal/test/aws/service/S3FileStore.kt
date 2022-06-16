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
package com.nortal.test.aws.service

import com.amazonaws.services.s3.transfer.MultipleFileUpload
import com.amazonaws.services.s3.transfer.TransferManager
import com.nortal.test.aws.configuration.AwsProperties
import com.nortal.test.core.exception.TestAutomationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.isDirectory

/**
 * Amazon S3 directory uploader.
 */
@Component
open class S3FileStore(
    private val awsProperties: AwsProperties,
    private val transferManager: TransferManager
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Upload file or directory.
     * @param dirPath directory to upload
     * @param targetDir a directory in bucket
     */
    open fun uploadDir(dirPath: Path, targetDir: String) {
        if (!dirPath.isDirectory()) {
            throw TestAutomationException("specified dir path [$dirPath] is not a directory!")
        }

        try {
            val fileTransferJob: MultipleFileUpload = transferManager.uploadDirectory(
                awsProperties.s3BucketName, targetDir, dirPath.toFile(), true
            )

            FileTransferProgressMonitor.showTransferProgress(fileTransferJob)
            FileTransferProgressMonitor.waitForCompletion(fileTransferJob)
        } catch (ex: Exception) {
            log.error("AWS upload failure", ex)
        }
    }


}