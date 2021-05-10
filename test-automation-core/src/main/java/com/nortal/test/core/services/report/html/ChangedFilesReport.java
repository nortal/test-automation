package com.nortal.test.core.services.report.html;

import java.util.List;
import lombok.Data;

@Data
public class ChangedFilesReport {
	private final List<Commit> commits;
	private final long sinceDays;

	public int size() {
		return commits.size();
	}

	public String getStats() {
		return String.format("There were %s commits to golden data during last %s days.\n", size(), sinceDays);
	}

}
