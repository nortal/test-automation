package com.nortal.test.core.services.report;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.nortal.test.core.configuration.ReportProperties;
import com.nortal.test.core.services.report.html.ChangedFilesReport;
import com.nortal.test.core.services.report.html.Commit;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.OrTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
@Component
public class FileHistoryService {
	private static final List<String> GD_PATHS = List.of(
			"dcp-test-automation-data/src/main/resources/plans/",
			"dcp-test-automation-data/src/main/resources/products/",
			"dcp-test-automation-data/src/main/resources/promotions/",
			"dcp-test-automation-data/src/main/resources/tradeinmodelfmv/");

	private final ReportProperties props;
	private Git git;

	public ChangedFilesReport getCommits() {
		if (!props.shouldIncludeGdHistory()) {
			return new ChangedFilesReport(Collections.emptyList(), 0);
		}

		try {
			final List<Commit> commits = commitStream()
					.filter(it -> it.getParentCount() == 1) //we skip merge commits
					.sorted(Comparator.comparingInt(RevCommit::getCommitTime).reversed())
					.map(this::toCommit)
					.collect(toList());

			return new ChangedFilesReport(commits, props.getGdHistoryDays());

		} catch (Exception e) {
			log.warn("Failed to process GD git changes.", e);
			return new ChangedFilesReport(Collections.emptyList(), props.getGdHistoryDays());
		}
	}

	@SneakyThrows
	private Stream<RevCommit> commitStream() {
		final long since = Instant.now().minus(props.getGdHistoryDays(), ChronoUnit.DAYS).toEpochMilli();
		final LogCommand log = getGit().log().setRevFilter(CommitTimeRevFilter.after(since));
		GD_PATHS.forEach(log::addPath);
		return StreamSupport.stream(log.call().spliterator(), false);
	}

	@SneakyThrows
	private Commit toCommit(final RevCommit commit) {
		try (ObjectReader reader = getGit().getRepository().newObjectReader()) {
			final var newTree = new CanonicalTreeParser(null, reader, commit.getTree().getId());
			final var oldTree = new CanonicalTreeParser(null, reader, commit.getParent(0).getTree().getId());
			final TreeFilter pathFilter = OrTreeFilter.create(GD_PATHS.stream().map(PathFilter::create).collect(toList()));

			final List<DiffEntry> diffs = getGit().diff()
												  .setNewTree(newTree)
												  .setOldTree(oldTree)
												  .setPathFilter(pathFilter)
												  .call();

			return new Commit(commit, diffs);
		}
	}

	@SneakyThrows
	private Git getGit() {
		if (git == null) {
			final Repository repo = new RepositoryBuilder().findGitDir().build();
			git = new Git(repo);
		}
		return git;
	}
}
