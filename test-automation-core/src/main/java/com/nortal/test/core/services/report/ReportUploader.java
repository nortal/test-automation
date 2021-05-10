package com.nortal.test.core.services.report;

import static com.nortal.test.core.rest.RetrofitCallHelper.safelyExecute;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.nortal.test.core.configuration.ReportProperties;
import com.nortal.test.core.rest.api.ReportUploaderApi;
import com.nortal.test.core.rest.error.RestCallException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import okio.GzipSink;
import okio.Okio;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReportUploader {

	private final ReportUploaderApi uploaderApi;
	private final ReportProperties reportProperties;
	@Value("${integration.hosts.report_upload:#{null}}")
	private String uploadHost;

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public ReportUploader(final Optional<ReportUploaderApi> uploaderApi,
						  final ReportProperties reportProperties) {
		this.uploaderApi = uploaderApi.orElse(null);
		this.reportProperties = reportProperties;
	}

	private static boolean shouldUpload(Path path) {
		final String fileName = path.getFileName().toString();
		//we do not want to upload embedding files as they are duplicated in report_feature files
		return !fileName.startsWith("embedding");
	}

	/**
	 * Uploads report files to the remote server for safekeeping.
	 */
	public void upload() {
		if (!reportProperties.shouldReport()) {
			log.info("Skipping report upload, in order to upload the report the following options have to be present and set:\n" +
							 "report.enable={}\n" +
							 "report.buildName={}\n" +
							 "report.buildNumber={}\n" +
							 "integration.hosts.report_upload={}", reportProperties.isEnabled(), reportProperties.getBuildName(),
					 reportProperties.getBuildNumber(), uploadHost);
			return;
		}

		log.info("Starting test report upload to remote server");
		final ExecutorService executorService = Executors.newFixedThreadPool(10);

		getReportFiles()
				.stream()
				.map(file -> executorService.submit(() -> upload(file)))
				.collect(Collectors.toList())
				.forEach(this::waitForIt);
	}

	private void waitForIt(Future<?> future) {
		try {
			future.get();
		} catch (InterruptedException | ExecutionException e) {
			log.warn("Failed to upload file", e);
		}
	}

	private List<File> getReportFiles() {
		try {
			final Path testReportFolder = Paths.get(reportProperties.getOutput()).resolve(Paths.get("cucumber-html-reports"));
			log.info("Collecting files from path: {}", testReportFolder);
			return Files.walk(testReportFolder)
						.filter(Files::isRegularFile)
						.filter(ReportUploader::shouldUpload)
						.map(Path::toFile)
						.collect(Collectors.toList());
		} catch (IOException e) {
			log.error("Failed to collect test report files to be uploaded", e);
			return Collections.emptyList();
		}
	}

	private void upload(File file) {
		final String name = file.getName();
		final String contentType = new Tika().detect(name);
		log.info("Uploading file {} with content-type {}", name, contentType);

		try (final Buffer zippedFileBuffer = new Buffer();
				final BufferedSource source = Okio.buffer(Okio.source(file));
				final GzipSink gzipSink = new GzipSink(zippedFileBuffer)) {

			//we transfer all the bytes of the file into the gzip sink
			source.readAll(gzipSink);
			//we then close the gzip sink to force it to deflate its contents which are held in zippedFileBuffer
			gzipSink.close();
			//we then read the zipped bytes into a ByteString to be passed in request body
			final ByteString content = zippedFileBuffer.readByteString();

			final RequestBody requestBody = RequestBody.create(MediaType.parse(contentType), content);
			final MultipartBody.Part body = MultipartBody.Part.createFormData("file", name, requestBody);
			safelyExecute(uploaderApi.upload(reportProperties.getBuildName(), reportProperties.getBuildNumber(), body));
		} catch (IOException | RestCallException e) {
			log.warn("failed to upload file {}", name);
			log.warn("", e);
		}
	}
}
