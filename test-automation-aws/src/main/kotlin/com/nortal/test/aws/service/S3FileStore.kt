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