package com.nortal.test.core.services.report.html;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;
import lombok.Data;
import lombok.Value;
import static java.time.temporal.ChronoUnit.DAYS;

@Data
public class Commit {
	private static final String ROOT_PATH = "dcp-test-automation-data/src/main/resources/";
	private final RevCommit commit;
	private final List<DiffEntry> diffsEntries;

	public String getAuthor() {
		return commit.getAuthorIdent().getName();
	}

	public String getAgo() {
		return DAYS.between(Instant.ofEpochSecond(commit.getCommitTime()), Instant.now()) + " days ago";
	}

	public String getSha() {
		return commit.getName();
	}

	public String getMessage() {
		return commit.getShortMessage();
	}

	public List<Diff> getDiffs() {
		return diffsEntries.stream()
				.map(Diff::new)
				.collect(Collectors.toList());
	}

	@Value
	public static class Diff {
		String path;
		String op;

		public Diff(final DiffEntry diff) {
			this.path = resolvePath(diff).replaceAll(ROOT_PATH, "");
			this.op = diff.getChangeType().toString();
		}

		private String resolvePath(final DiffEntry diff) {
			switch (diff.getChangeType()) {
			case ADD:
				return diff.getNewPath();
			case COPY:
			case RENAME:
				return diff.getOldPath() + "->" + diff.getNewPath();
			case DELETE:
			case MODIFY:
				return diff.getOldPath();
			default:
				throw new IllegalStateException("Unexpected value: " + diff.getChangeType());
			}
		}

	}

}
