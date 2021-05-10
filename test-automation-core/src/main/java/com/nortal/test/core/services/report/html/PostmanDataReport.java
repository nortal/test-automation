package com.nortal.test.core.services.report.html;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostmanDataReport {
	private final boolean enabled;
	private final List<CollectionData> collections;
	private final List<String> environments;

	@Data
	@Builder
	public static class CollectionData {
		private final String id;
		private final String name;
		private final String fileName;
	}
}
