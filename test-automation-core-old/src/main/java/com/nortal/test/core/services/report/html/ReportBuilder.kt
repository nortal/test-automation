package com.nortal.test.core.services.report.html

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import net.masterthought.cucumber.*
import net.masterthought.cucumber.ReportBuilder
import net.masterthought.cucumber.generators.*
import net.masterthought.cucumber.json.support.TagObject
import net.masterthought.cucumber.json.Feature
import org.apache.commons.io.FileUtils
import org.assertj.core.util.Lists
import java.io.*
import java.lang.Exception
import java.nio.charset.StandardCharsets
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Copy of [net.masterthought.cucumber.ReportBuilder]. This is a clean version, we're just changing method visibility to allow extensions.
 */
open class ReportBuilder(private val jsonFiles: List<String>, protected var configuration: Configuration) {
    protected var reportResult: ReportResult? = null
    private val reportParser: ReportParser

    /**
     * Flag used to detect if the file with updated trends is saved. If the report crashes and the trends was not saved then it tries to save trends
     * again with empty data to mark that the build crashed.
     */
    private var wasTrendsFileSaved = false

    init {
        reportParser = ReportParser(configuration)
    }

    /**
     * Parses provided files and generates the report. When generating process fails report with information about error is provided.
     *
     * @return stats for the generated report
     */
    fun generateReports(): Reportable? {
        var trends: Trends? = null
        return try {
            // first copy static resources so ErrorPage is displayed properly
            copyStaticResources()

            // create directory for embeddings before files are generated
            createEmbeddingsDirectory()

            // add metadata info sourced from files
            reportParser.parseClassificationsFiles(configuration.classificationFiles)

            // parse json files for results
            var features: List<Feature?>? = Lists.newArrayList()
            try {
                features = reportParser.parseJsonFiles(jsonFiles)
            } catch (ve: ValidationException) {
                LOG.log(Level.SEVERE, "There has been a ValidationException while parsing json files in output directory.", ve)
            }
            reportResult = ReportResult(features, configuration)
            val reportable = reportResult!!.featureReport
            if (configuration.isTrendsAvailable) {
                // prepare data required by generators, collect generators and generate pages
                trends = updateAndSaveTrends(reportable)
            }

            // Collect and generate pages in a single pass
            generatePages(trends)
            reportable

            // whatever happens we want to provide at least error page instead of incomplete report or exception
        } catch (e: Exception) {
            generateErrorPage(e)
            // update trends so there is information in history that the build failed

            // if trends was not created then something went wrong
            // and information about build failure should be saved
            if (!wasTrendsFileSaved && configuration.isTrendsAvailable) {
                val reportable: Reportable = EmptyReportable()
                updateAndSaveTrends(reportable)
            }

            // something went wrong, don't pass result that might be incomplete
            null
        }
    }

    protected open fun copyStaticResources() {
        copyResources("css", "cucumber.css", "bootstrap.min.css", "font-awesome.min.css")
        copyResources(
            "js", "jquery.min.js", "jquery.tablesorter.min.js", "bootstrap.min.js", "Chart.min.js",
            "moment.min.js"
        )
        copyResources(
            "fonts", "FontAwesome.otf", "fontawesome-webfont.svg", "fontawesome-webfont.woff",
            "fontawesome-webfont.eot", "fontawesome-webfont.ttf", "fontawesome-webfont.woff2",
            "glyphicons-halflings-regular.eot", "glyphicons-halflings-regular.eot",
            "glyphicons-halflings-regular.woff2", "glyphicons-halflings-regular.woff",
            "glyphicons-halflings-regular.ttf", "glyphicons-halflings-regular.svg"
        )
        copyResources("images", "favicon.png")
    }

    private fun createEmbeddingsDirectory() {
        configuration.embeddingDirectory.mkdirs()
    }

    protected fun copyResources(resourceLocation: String, vararg resources: String) {
        for (resource in resources) {
            val tempFile = File(
                configuration.reportDirectory.absoluteFile,
                BASE_DIRECTORY + File.separatorChar + resourceLocation + File.separatorChar + resource
            )
            // don't change this implementation unless you verified it works on Jenkins
            try {
                FileUtils.copyInputStreamToFile(
                    this.javaClass.getResourceAsStream("/$resourceLocation/$resource"), tempFile
                )
            } catch (e: IOException) {
                // based on FileUtils implementation, should never happen even is declared
                throw ValidationException(e)
            }
        }
    }

    protected open fun generatePages(trends: Trends?) {
        for (feature in reportResult!!.allFeatures) {
            FeatureReportPage(reportResult, configuration, feature).generatePage()
        }
        TagsOverviewPage(reportResult, configuration).generatePage()
        for (tagObject in reportResult!!.allTags) {
            TagReportPage(reportResult, configuration, tagObject).generatePage()
        }
        StepsOverviewPage(reportResult, configuration).generatePage()
        FailuresOverviewPage(reportResult, configuration).generatePage()
        if (configuration.isTrendsAvailable) {
            TrendsOverviewPage(reportResult, configuration, trends).generatePage()
        }
    }

    private fun updateAndSaveTrends(reportable: Reportable): Trends {
        val trends = loadOrCreateTrends()
        appendToTrends(trends, reportable)

        // display only last n items - don't skip items if limit is not defined
        if (configuration.trendsLimit > 0) {
            trends.limitItems(configuration.trendsLimit)
        }

        // save updated trends so it contains history only for the last builds
        saveTrends(trends, configuration.trendsStatsFile)
        return trends
    }

    private fun loadOrCreateTrends(): Trends {
        val trendsFile = configuration.trendsStatsFile
        return if (trendsFile != null && trendsFile.exists()) {
            loadTrends(trendsFile)
        } else {
            Trends()
        }
    }

    private fun appendToTrends(trends: Trends, result: Reportable) {
        trends.addBuild(configuration.buildNumber, result)
    }

    private fun saveTrends(trends: Trends, file: File) {
        val objectWriter = mapper.writer().with(SerializationFeature.INDENT_OUTPUT)
        try {
            OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8).use { writer ->
                objectWriter.writeValue(writer, trends)
                wasTrendsFileSaved = true
            }
        } catch (e: IOException) {
            wasTrendsFileSaved = false
            throw ValidationException("Could not save updated trends in file: " + file.absolutePath, e)
        }
    }

    private fun generateErrorPage(exception: Exception) {
        LOG.log(Level.INFO, "Unexpected error", exception)
        val errorPage = ErrorPage(reportResult, configuration, exception, jsonFiles)
        errorPage.generatePage()
    }

    companion object {
        private val LOG = Logger.getLogger(ReportBuilder::class.java.name)

        /**
         * Page that should be displayed when the reports is generated. Shared between [FeaturesOverviewPage] and [ErrorPage].
         */
        const val HOME_PAGE = "overview-features.html"

        /**
         * Subdirectory where the report will be created.
         */
        const val BASE_DIRECTORY = "cucumber-html-reports"
        private val mapper = ObjectMapper()
        private fun loadTrends(file: File): Trends {
            try {
                InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8).use { reader -> return mapper.readValue(reader, Trends::class.java) }
            } catch (e: JsonMappingException) {
                throw ValidationException(String.format("File '%s' could not be parsed as file with trends!", file), e)
            } catch (e: IOException) {
                // IO problem - stop generating and re-throw the problem
                throw ValidationException(e)
            }
        }
    }
}