package com.nortal.test.core.model.solr;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SolrDeleteRequest {
	private Delete delete;

	@Data
	@Builder
	public static class Delete {
		private String query;
	}
}
