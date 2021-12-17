package com.nortal.test.core.services.report

import com.nortal.test.core.configuration.ReportProperties
import com.nortal.test.core.rest.RetrofitCallHelper
import com.nortal.test.core.rest.RetrofitCallHelper.safelyExecute
import com.nortal.test.core.rest.api.ReportUploaderApi
import com.nortal.test.core.rest.error.RestCallException
import lombok.extern.slf4j.Slf4j
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer
import okio.GzipSink
import okio.Okio
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.lang.Runnable
import java.lang.InterruptedException
import java.io.IOException
import org.apache.tika.Tika
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.lang.Void
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.function.Consumer
import java.util.stream.Collectors

@Slf4j
@Component
class ReportUploader(
    uploaderApi: Optional<ReportUploaderApi?>,
    reportProperties: ReportProperties, retrofitCallHelper: RetrofitCallHelper
) {
    private val uploaderApi: ReportUploaderApi?
    private val reportProperties: ReportProperties
    private val retrofitCallHelper: RetrofitCallHelper

    @Value("\${integration.hosts.report_upload:#{null}}")
    private val uploadHost: String? = null

    init {
        this.uploaderApi = uploaderApi.orElse(null)
        this.reportProperties = reportProperties
        this.retrofitCallHelper = retrofitCallHelper
    }

    /**
     * Uploads report files to the remote server for safekeeping.
     */
    fun upload() {
        if (!reportProperties.shouldReport()) {
            ReportUploader.log.info(
                """
    Skipping report upload, in order to upload the report the following options have to be present and set:
    report.enable={}
    report.buildName={}
    report.buildNumber={}
    integration.hosts.report_upload={}
    """.trimIndent(), reportProperties.isEnabled, reportProperties.buildName,
                reportProperties.buildNumber, uploadHost
            )
            return
        }
        ReportUploader.log.info("Starting test report upload to remote server")
        val executorService = Executors.newFixedThreadPool(10)
        reportFiles
            .stream()
            .map { file: File -> executorService.submit { upload(file) } }
            .collect(Collectors.toList())
            .forEach(Consumer { future: Future<Any?> -> waitForIt(future) })
    }

    private fun waitForIt(future: Future<*>) {
        try {
            future.get()
        } catch (e: InterruptedException) {
            ReportUploader.log.warn("Failed to upload file", e)
        } catch (e: ExecutionException) {
            ReportUploader.log.warn("Failed to upload file", e)
        }
    }

    private val reportFiles: List<File>
        private get() = try {
            val testReportFolder = Paths.get(reportProperties.output).resolve(Paths.get("cucumber-html-reports"))
            ReportUploader.log.info("Collecting files from path: {}", testReportFolder)
            Files.walk(testReportFolder)
                .filter { path: Path? -> Files.isRegularFile(path) }
                .filter { path: Path -> shouldUpload(path) }
                .map { obj: Path -> obj.toFile() }
                .collect(Collectors.toList())
        } catch (e: IOException) {
            ReportUploader.log.error("Failed to collect test report files to be uploaded", e)
            emptyList()
        }

    private fun upload(file: File) {
        val name = file.name
        val contentType = Tika().detect(name)
        ReportUploader.log.info("Uploading file {} with content-type {}", name, contentType)
        try {
            Buffer().use { zippedFileBuffer ->
                Okio.buffer(Okio.source(file)).use { source ->
                    GzipSink(zippedFileBuffer).use { gzipSink ->

                        //we transfer all the bytes of the file into the gzip sink
                        source.readAll(gzipSink)
                        //we then close the gzip sink to force it to deflate its contents which are held in zippedFileBuffer
                        gzipSink.close()
                        //we then read the zipped bytes into a ByteString to be passed in request body
                        val content = zippedFileBuffer.readByteString()
                        val requestBody = RequestBody.create(MediaType.parse(contentType), content)
                        val body = MultipartBody.Part.createFormData("file", name, requestBody)
                        retrofitCallHelper.safelyExecute(uploaderApi!!.upload(reportProperties.buildName, reportProperties.buildNumber, body))
                    }
                }
            }
        } catch (e: IOException) {
            ReportUploader.log.warn("failed to upload file {}", name)
            ReportUploader.log.warn("", e)
        } catch (e: RestCallException) {
            ReportUploader.log.warn("failed to upload file {}", name)
            ReportUploader.log.warn("", e)
        }
    }

    companion object {
        private fun shouldUpload(path: Path): Boolean {
            val fileName = path.fileName.toString()
            //we do not want to upload embedding files as they are duplicated in report_feature files
            return !fileName.startsWith("embedding")
        }
    }
}