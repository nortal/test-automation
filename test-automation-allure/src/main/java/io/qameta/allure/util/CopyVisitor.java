
package io.qameta.allure.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * NOTE: It's a copy of allure internal class and solves ZiPath issues.
 * Visitor that recursive copies directories.
 *
 * @since 2.0
 */
public class CopyVisitor extends SimpleFileVisitor<Path> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CopyVisitor.class);

	private final Path sourceDirectory;

	private final Path outputDirectory;

	public CopyVisitor(final Path sourceDirectory, final Path outputDirectory) {
		this.sourceDirectory = sourceDirectory;
		this.outputDirectory = outputDirectory;
	}

	@Override
	public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			LOGGER.error("Could not create directory", e);
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
		if (Files.notExists(file)) {
			return FileVisitResult.CONTINUE;
		}
		final Path dest = outputDirectory.resolve(file.getFileName().toString());
		try {
			Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			LOGGER.error("Could not copy file", e);
		}
		return FileVisitResult.CONTINUE;
	}
}
