package com.nortal.test.core.services.report.html

import lombok.Data
import lombok.Value
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.revwalk.RevCommit
import java.time.temporal.ChronoUnit
import java.lang.IllegalStateException
import java.time.Instant
import java.util.stream.Collectors

@Data
class Commit {
    private val commit: RevCommit? = null
    private val diffsEntries: List<DiffEntry>? = null
    val author: String
        get() = commit!!.authorIdent.name
    val ago: String
        get() = ChronoUnit.DAYS.between(Instant.ofEpochSecond(commit!!.commitTime.toLong()), Instant.now()).toString() + " days ago"
    val sha: String
        get() = commit!!.name
    val message: String
        get() = commit!!.shortMessage
    val diffs: List<Diff>
        get() = diffsEntries!!.stream()
            .map { diff: DiffEntry -> Diff(diff) }
            .collect(Collectors.toList())

    @Value
    class Diff(diff: DiffEntry) {
        var path: String
        var op: String

        init {
            path = resolvePath(diff).replace(ROOT_PATH.toRegex(), "")
            op = diff.changeType.toString()
        }

        private fun resolvePath(diff: DiffEntry): String {
            return when (diff.changeType) {
                DiffEntry.ChangeType.ADD -> diff.newPath
                DiffEntry.ChangeType.COPY, DiffEntry.ChangeType.RENAME -> diff.oldPath + "->" + diff.newPath
                DiffEntry.ChangeType.DELETE, DiffEntry.ChangeType.MODIFY -> diff.oldPath
                else -> throw IllegalStateException("Unexpected value: " + diff.changeType)
            }
        }
    }

    companion object {
        private const val ROOT_PATH = "dcp-test-automation-data/src/main/resources/"
    }
}