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