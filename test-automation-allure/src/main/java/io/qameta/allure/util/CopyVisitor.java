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
 * <b>NOTE: It's a copy of allure internal class and solves ZiPath issues.</b>
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
