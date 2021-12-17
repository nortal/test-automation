package com.nortal.test.core.testng

import com.nortal.test.core.configuration.ScenarioProperties
import com.nortal.test.core.configuration.TestConfiguration
import com.nortal.test.core.model.Tags
import com.nortal.test.core.plugin.CucumberScenarioNameProvider
import com.nortal.test.core.plugin.TestSourcesModel
import com.nortal.test.core.services.hooks.AfterSuiteHook
import com.nortal.test.core.services.hooks.BeforeTestRunnerHook
import com.nortal.test.core.services.hooks.HookContext
import com.nortal.test.core.services.report.ReportGenerator
import com.nortal.test.core.services.report.ScenarioSkipService
import io.cucumber.plugin.event.TestSourceRead
import io.cucumber.testng.FeatureWrapper
import io.cucumber.testng.FeatureWrapperImpl
import io.cucumber.testng.PickleWrapper
import io.cucumber.testng.TestNGCucumberRunner
import lombok.extern.slf4j.Slf4j
import org.apache.commons.lang3.ArrayUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests
import org.testng.annotations.*
import java.time.Instant
import java.util.*
import java.util.stream.Collectors

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = [TestConfiguration::class])
abstract class AbstractTestNGIntegrationTests : AbstractTestNGSpringContextTests(), SpringContextHolder {
    private val testSources = TestSourcesModel()
    private val workspaceManager = WorkspaceManager()
    private var testNGCucumberRunner: TestNGCucumberRunner? = null

    @Autowired
    private val skipService: ScenarioSkipService? = null

    @Autowired
    private val reportGenerator: ReportGenerator? = null

    @Autowired(required = false)
    private val beforeTestRunnerHooks = emptyList<BeforeTestRunnerHook>()

    @Autowired(required = false)
    private val afterSuiteHooks = emptyList<AfterSuiteHook>()

    @Autowired
    private val scenarioProperties: ScenarioProperties? = null

    @BeforeSuite
    fun beforeSuiteHook() {
        workspaceManager.prepareWorkspace()
        val currentThreadCount = System.getProperty(KEY_THREAD_COUNT)
        if (currentThreadCount == null) {
            System.setProperty(KEY_THREAD_COUNT, "5")
        }
    }

    @BeforeClass
    fun beforeClassHook() {
        System.setProperty("cucumber.object-factory", SingleExistingContextSpringFactory::class.java.name)
        System.setProperty("cucumber.features", scenarioProperties!!.domain.featurePath)
        SingleExistingContextSpringFactory.CONTEXT_HOLDER = this
        testNGCucumberRunner = TestNGCucumberRunner(this.javaClass)
        val hookContext = HookContext(go)
            .goldenDataLockIsMandatory(isGoldenDataLockMandatory && ArrayUtils.isNotEmpty(scenarios()))
            .build()
        beforeTestRunnerHooks.stream()
            .sorted(Comparator.comparingInt { obj: BeforeTestRunnerHook -> obj.beforeTestRunnerOrder() })
            .peek { it: BeforeTestRunnerHook ->
                log.info(
                    "Running before test runner hook for: {}",
                    it.javaClass.name
                )
            }
            .forEach { hook: BeforeTestRunnerHook -> hook.beforeTestRunner(hookContext) }
    }

    val isGoldenDataLockMandatory: Boolean
        get() = false

    @Test(groups = ["cucumber"], description = "Runs Cucumber Scenarios", dataProvider = "scenarios")
    @Throws(Throwable::class)
    fun runScenario(pickleWrapper: PickleWrapper, featureWrapper: FeatureWrapper) {
        try {
            updateThreadLocalFeatureInfo(pickleWrapper, featureWrapper)
            testNGCucumberRunner!!.runScenario(pickleWrapper.pickle)
        } finally {
            CucumberScenarioNameProvider.instance.clearCurrentFeatureInfo()
        }
    }

    private fun updateThreadLocalFeatureInfo(pickleWrapper: PickleWrapper, featureWrapper: FeatureWrapper) {
        val path = pickleWrapper.pickle.uri.toString()
        val source = (featureWrapper as FeatureWrapperImpl).feature.source
        testSources.addTestSourceReadEvent(path, TestSourceRead(Instant.now(), pickleWrapper.pickle.uri, source))
        CucumberScenarioNameProvider.instance.setFeatureInfo(testSources.getFeature(path), path)
    }

    @DataProvider
    fun scenarios(): Array<out Any> {
        val filteredScenarios = Arrays.stream(testNGCucumberRunner!!.provideScenarios())
            .filter { scenario: Array<Any> -> this.filter(scenario) }
            .toArray()
        if (shuffleScenarios()) {
            ArrayUtils.shuffle(filteredScenarios)
        }
        return filteredScenarios
    }

    private fun filter(scenario: Array<Any>): Boolean {
        val pickle = scenario[0] as PickleWrapper
        val feature = scenario[1] as FeatureWrapper
        skipService!!.collectSkippedScenarios(feature, pickle)
        val tags = pickle.pickle.tags
        return filterByTag(tags)
    }

    /**
     * Allows test runner to choose whether to include a scenario according to its tags.
     *
     * @param tags of the scenario
     * @return true if include false if not
     */
    fun filterByTag(tags: List<String>): Boolean {
        return tags.stream().noneMatch { anObject: String? -> Tags.SKIP.name.equals(anObject) } && isCorrectScope(tags)
    }

    private fun isCorrectScope(tags: List<String>): Boolean {
        val scopeTags = tags.stream()
            .map { obj: String -> obj.uppercase(Locale.getDefault()) }
            .filter { tag: String -> tag.startsWith("@SCOPE:") }
            .collect(Collectors.toList())
        check(scopeTags.size <= 1) { "Scenarios can not be tagged with more than one scope tag! $scopeTags" }
        val scope = scenarioProperties!!.scope
        return when (scope) {
            ScenarioProperties.Scope.MOCKED -> scopeTags.isEmpty() || scope.tags.contains(scopeTags[0])
            ScenarioProperties.Scope.E2E -> !scopeTags.isEmpty() && scope.tags.contains(scopeTags[0])
            else -> throw IllegalStateException("Unexpected value: " + scenarioProperties.scope)
        }
    }

    @AfterClass(alwaysRun = true)
    fun tearDownClass() {
        if (testNGCucumberRunner == null) {
            return
        }
        testNGCucumberRunner!!.finish()
    }

    @AfterSuite(alwaysRun = true)
    fun afterSuiteHook() {
        afterSuiteHooks.stream()
            .sorted(Comparator.comparingInt { obj: AfterSuiteHook -> obj.afterSuitOrder() })
            .peek { it: AfterSuiteHook -> log.info("Running after suite hook for: {}", it.javaClass.name) }
            .forEach { hook: AfterSuiteHook -> executeHook(hook) }
        workspaceManager.cleanupWorkspace()
        try {
            reportGenerator!!.generate()
        } catch (e: Exception) {
            log.error("Error while generating report", e)
        }
    }

    /**
     * This is to override the default behaviour of shuffling the scenarios before executing them. Shuffling is done to ensure even mix of
     * features is
     * run in parallel to minimize/avoid bottlenecks do to simultaneous executions of similar flows
     *
     * @return true by default
     */
    protected fun shuffleScenarios(): Boolean {
        return true
    }

    override val applicationContext: ConfigurableApplicationContext?
        get() = this.applicationContext as ConfigurableApplicationContext?

    @Throws(Exception::class)
    override fun beforeTestClass() {
        super.springTestContextBeforeTestClass()
    }

    @Throws(Exception::class)
    override fun afterTestClass() {
        super.springTestContextAfterTestClass()
    }

    companion object {
        private const val KEY_THREAD_COUNT = "dataproviderthreadcount"
        private val log: Logger = LoggerFactory.getLogger(AbstractTestNGIntegrationTests::class.java)

        private fun executeHook(hook: AfterSuiteHook) {
            try {
                hook.afterSuite()
            } catch (e: Exception) {
                log.error("After suite hook failed with error.", e)
            }
        }
    }
}