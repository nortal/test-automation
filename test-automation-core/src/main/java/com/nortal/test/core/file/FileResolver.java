package com.nortal.test.core.file;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

@Slf4j
@Component
public class FileResolver {
	private final ResourceLoader resourceLoader;

	public FileResolver(final ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public String getFileAsString(final String filepath) {
		return asString(getResource(filepath));
	}

	private String asString(Resource resource) {
		try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
			return FileCopyUtils.copyToString(reader);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private Resource getResource(final String filepath) {
		return ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResource(filepath);
	}

}
