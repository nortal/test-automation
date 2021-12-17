package com.nortal.test.core.services.report

import com.nortal.test.core.configuration.ReportProperties
import com.nortal.test.core.services.report.html.ChangedFilesReport
import com.nortal.test.core.services.report.html.Commit
import lombok.RequiredArgsConstructor
import java.util.function.ToIntFunction
import lombok.SneakyThrows
import lombok.extern.slf4j.Slf4j
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.RepositoryBuilder
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.filter.OrTreeFilter
import org.eclipse.jgit.treewalk.filter.PathFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.Exception
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Comparator
import java.util.List
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.stream.StreamSupport

@Slf4j
@RequiredArgsConstructor
@Component
class FileHistoryService {
    private val props: ReportProperties? = null
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @get:SneakyThrows
    private var git: Git? = null
        private get() {
            if (field == null) {
                val repo = RepositoryBuilder().findGitDir().build()
                field = Git(repo)
            }
            return field
        }

    //we skip merge commits
    val commits: ChangedFilesReport
        get() = if (!props!!.shouldIncludeGdHistory()) {
            ChangedFilesReport(emptyList<Any>(), 0)
        } else try {
            val commits = commitStream()
                .filter { it: RevCommit -> it.parentCount == 1 } //we skip merge commits
                .sorted(Comparator.comparingInt { obj: RevCommit -> obj.commitTime }
                    .reversed())
                .map { commit: RevCommit -> toCommit(commit) }
                .collect(Collectors.toList())
            ChangedFilesReport(commits, props.gdHistoryDays)
        } catch (e: Exception) {
            FileHistoryService.log.warn("Failed to process GD git changes.", e)
            ChangedFilesReport(emptyList<Any>(), props.gdHistoryDays)
        }

    @SneakyThrows
    private fun commitStream(): Stream<RevCommit> {
        val since = Instant.now().minus(props!!.gdHistoryDays.toLong(), ChronoUnit.DAYS).toEpochMilli()
        val log = git!!.log().setRevFilter(CommitTimeRevFilter.after(since))
        GD_PATHS.forEach(Consumer { path: String? -> log.addPath(path) })
        return StreamSupport.stream(log.call().spliterator(), false)
    }

    @SneakyThrows
    private fun toCommit(commit: RevCommit): Commit {
        git!!.repository.newObjectReader().use { reader ->
            val newTree = CanonicalTreeParser(null, reader, commit.tree.id)
            val oldTree = CanonicalTreeParser(null, reader, commit.getParent(0).tree.id)
            val pathFilter = OrTreeFilter.create(GD_PATHS.stream().map { path: String? -> PathFilter.create(path) }
                .collect(Collectors.toList()))
            val diffs = git!!.diff()
                .setNewTree(newTree)
                .setOldTree(oldTree)
                .setPathFilter(pathFilter)
                .call()
            return Commit(commit, diffs)
        }
    }

    companion object {
        private val GD_PATHS = List.of(
            "dcp-test-automation-data/src/main/resources/plans/",
            "dcp-test-automation-data/src/main/resources/products/",
            "dcp-test-automation-data/src/main/resources/promotions/",
            "dcp-test-automation-data/src/main/resources/tradeinmodelfmv/"
        )
    }
}