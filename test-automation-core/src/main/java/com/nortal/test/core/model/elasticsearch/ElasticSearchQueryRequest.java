package com.nortal.test.core.model.elasticsearch;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
@Builder
public class ElasticSearchQueryRequest {
	private Query query;

	@Data
	@Builder
	public static class Query {
		private Bool bool;
	}

	@Data
	@Builder
	public static class Bool {
		private Filter filter;
	}

	@Data
	@Builder
	public static class Filter {
		private Map<String, Set<String>> terms;
	}
}
