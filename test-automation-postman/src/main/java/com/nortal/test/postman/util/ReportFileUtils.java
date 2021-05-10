package com.nortal.test.postman.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;

/**
 * File management convenience utils
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReportFileUtils {

	public static void createDirectory(final String path) {
		new File(path).mkdirs();
	}

	public static List<String> listFileNames(final String path) {
		return Optional.ofNullable(new File(path).list())
				.map(Arrays::asList)
				.orElse(Collections.emptyList());
	}

	public static void zipDirectory(final String path, final String outputFile, final Function<String, String> entryPrefixResolver) {
		try (FileOutputStream fos = new FileOutputStream(outputFile); ZipOutputStream zos = new ZipOutputStream(fos)) {
			final File directory = new File(path);
			addDirectoryToZip(directory, zos, entryPrefixResolver);
		} catch (IOException e) {
			throw new RuntimeException("Error archiving path: " + path, e);
		}
	}

	private static void addDirectoryToZip(final File directory, final ZipOutputStream zipOutputStream,
										  final Function<String, String> entryPrefixResolver) throws IOException {
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				addDirectoryToZip(file, zipOutputStream, entryPrefixResolver);
			} else {
				addFileToZip(file, zipOutputStream, entryPrefixResolver);
			}
		}
	}

	private static void addFileToZip(final File file, final ZipOutputStream zipOutputStream,
									 final Function<String, String> entryPrefixResolver) throws IOException {
		try (final FileInputStream fis = new FileInputStream(file)) {
			final String entryName = entryPrefixResolver.apply(file.getName()) + "/" + file.getName();
			final ZipEntry zipEntry = new ZipEntry(entryName);
			zipOutputStream.putNextEntry(zipEntry);
			IOUtils.copy(fis, zipOutputStream);
		}
	}
}
