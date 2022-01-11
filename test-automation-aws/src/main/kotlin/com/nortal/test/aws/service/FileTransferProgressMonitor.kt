package com.nortal.test.aws.service

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.transfer.Transfer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object FileTransferProgressMonitor {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun waitForCompletion(transfer: Transfer) {
        try {
            transfer.waitForCompletion()
        } catch (e: AmazonServiceException) {
            log.error("Amazon service error: " + e.message)
        } catch (e: AmazonClientException) {
            log.error("Amazon client error: " + e.message)
        } catch (e: InterruptedException) {
            log.error("Transfer interrupted: " + e.message)
        }
    }

    fun showTransferProgress(transfer: Transfer) {
        do {
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                return
            }

            val transferProgress = transfer.progress
            log.info(
                "Transferring data.. {}% completed. Data: {}/{} bytes.",
                transferProgress.percentTransferred,
                transferProgress.bytesTransferred,
                transferProgress.totalBytesToTransfer
            )
        } while (!transfer.isDone)

        log.info("Transfer job is complete. Final state: {}", transfer.state)
    }


}