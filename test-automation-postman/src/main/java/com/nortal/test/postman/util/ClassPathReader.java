package com.nortal.test.postman.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ClassPathReader {
	private final ResourceLoader resourceLoader;

	public Resource readResource(final String fileLocation) {
		return ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
				.getResource(fileLocation);
	}
}
