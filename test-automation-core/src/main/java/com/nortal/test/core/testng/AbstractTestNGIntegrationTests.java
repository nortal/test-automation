package com.nortal.test.core.testng;

import com.nortal.test.core.configuration.ScenarioProperties;
import com.nortal.test.core.configuration.TestConfiguration;
import com.nortal.test.core.model.Tags;
import com.nortal.test.core.plugin.CucumberScenarioNameProvider;
import com.nortal.test.core.plugin.TestSourcesModel;
import com.nortal.test.core.services.hooks.AfterSuiteHook;
import com.nortal.test.core.services.hooks.BeforeTestRunnerHook;
import com.nortal.test.core.services.hooks.HookContext;
import com.nortal.test.core.services.report.PostmanAutomationCollectionGenerator;
import com.nortal.test.core.services.report.ReportGenerator;
import com.nortal.test.core.services.report.ScenarioSkipService;
import io.cucumber.plugin.event.TestSourceRead;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.FeatureWrapperImpl;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {TestConfiguration.class})
public abstract class AbstractTestNGIntegrationTests extends AbstractTestNGSpringContextTests implements SpringContextHolder {
	private static final String KEY_THREAD_COUNT = "dataproviderthreadcount";

	private final TestSourcesModel testSources = new TestSourcesModel();
	private final WorkspaceManager workspaceManager = new WorkspaceManager();
	private TestNGCucumberRunner testNGCucumberRunner;

	@Autowired
	private ScenarioSkipService skipService;

	@Autowired
	private ReportGenerator reportGenerator;

	@Autowired
	private PostmanAutomationCollectionGenerator postmanAutomationCollectionGenerator;

	@Autowired(required = false)
	private List<BeforeTestRunnerHook> beforeTestRunnerHooks = Collections.emptyList();

	@Autowired(required = false)
	private List<AfterSuiteHook> afterSuiteHooks = Collections.emptyList();

	@Autowired
	private ScenarioProperties scenarioProperties;

	@BeforeSuite
	public void beforeSuiteHook() {
		workspaceManager.prepareWorkspace();

		String currentThreadCount = System.getProperty(KEY_THREAD_COUNT);
		if (currentThreadCount == null) {
			System.setProperty(KEY_THREAD_COUNT, "5");
		}
	}

	@BeforeClass
	public void beforeClassHook() {
		System.setProperty("cucumber.object-factory", SingleExistingContextSpringFactory.class.getName());
		System.setProperty("cucumber.features", scenarioProperties.getDomain().getFeaturePath());
		SingleExistingContextSpringFactory.CONTEXT_HOLDER = this;
		testNGCucumberRunner = new TestNGCucumberRunner(this.getClass());

		var hookContext = HookContext.builder()
				.goldenDataLockIsMandatory(isGoldenDataLockMandatory() && ArrayUtils.isNotEmpty(scenarios()))
				.build();

		beforeTestRunnerHooks.stream()
				.sorted(comparingInt(BeforeTestRunnerHook::beforeTestRunnerOrder))
				.peek(it -> log.info("Running before test runner hook for: {}", it.getClass().getName()))
				.forEach(hook -> hook.beforeTestRunner(hookContext));
	}

	public boolean isGoldenDataLockMandatory() {
		return false;
	}

	@Test(groups = "cucumber", description = "Runs Cucumber Scenarios", dataProvider = "scenarios")
	public void runScenario(final PickleWrapper pickleWrapper, final FeatureWrapper featureWrapper) throws Throwable {
		try {
			updateThreadLocalFeatureInfo(pickleWrapper, featureWrapper);
			testNGCucumberRunner.runScenario(pickleWrapper.getPickle());
		} finally {
			CucumberScenarioNameProvider.getInstance().clearCurrentFeatureInfo();
		}
	}

	private void updateThreadLocalFeatureInfo(final PickleWrapper pickleWrapper, final FeatureWrapper featureWrapper) {
		final String path = pickleWrapper.getPickle().getUri().toString();
		final String source = ((FeatureWrapperImpl) featureWrapper).getFeature().getSource();
		testSources.addTestSourceReadEvent(path, new TestSourceRead(Instant.now(), pickleWrapper.getPickle().getUri(), source));
		CucumberScenarioNameProvider.getInstance().setFeatureInfo(testSources.getFeature(path), path);
	}

	@DataProvider
	public Object[][] scenarios() {
		Object[][] filteredScenarios = Arrays.stream(testNGCucumberRunner.provideScenarios())
				.filter(this::filter)
				.toArray(Object[][]::new);

		if (shuffleScenarios()) {
			ArrayUtils.shuffle(filteredScenarios);
		}
		return filteredScenarios;
	}

	private boolean filter(final Object[] scenario) {
		final PickleWrapper pickle = (PickleWrapper) scenario[0];
		final FeatureWrapper feature = (FeatureWrapper) scenario[1];
		skipService.collectSkippedScenarios(feature, pickle);
		final List<String> tags = pickle.getPickle().getTags();
		return filterByTag(tags);
	}

	/**
	 * Allows test runner to choose whether to include a scenario according to its tags.
	 *
	 * @param tags of the scenario
	 * @return true if include false if not
	 */
	public boolean filterByTag(List<String> tags) {
		return tags.stream().noneMatch(Tags.SKIP.getName()::equals) && isCorrectScope(tags);
	}

	private boolean isCorrectScope(final List<String> tags) {
		final List<String> scopeTags = tags.stream()
				.map(String::toUpperCase)
				.filter(tag -> tag.startsWith("@SCOPE:"))
				.collect(Collectors.toList());

		if (scopeTags.size() > 1) {
			throw new IllegalStateException("Scenarios can not be tagged with more than one scope tag! " + scopeTags);
		}

		final var scope = scenarioProperties.getScope();
		switch (scope) {
			case MOCKED:
				return scopeTags.isEmpty() || scope.getTags().contains(scopeTags.get(0));
			case E2E:
				return !scopeTags.isEmpty() && scope.getTags().contains(scopeTags.get(0));
			default:
				throw new IllegalStateException("Unexpected value: " + scenarioProperties.getScope());
		}
	}

	@AfterClass(alwaysRun = true)
	public void tearDownClass() {
		if (testNGCucumberRunner == null) {
			return;
		}
		testNGCucumberRunner.finish();
	}

	@AfterSuite(alwaysRun = true)
	public void afterSuiteHook() {
		afterSuiteHooks.stream()
				.sorted(comparingInt(AfterSuiteHook::afterSuitOrder))
				.peek(it -> log.info("Running after suite hook for: {}", it.getClass().getName()))
				.forEach(AbstractTestNGIntegrationTests::executeHook);
		workspaceManager.cleanupWorkspace();

		try {
			postmanAutomationCollectionGenerator.generate();
			reportGenerator.generate();
		} catch (Exception e) {
			log.error("Error while generating report", e);
		}
	}

	private static void executeHook(AfterSuiteHook hook) {
		try {
			hook.afterSuite();
		} catch (Exception e) {
			log.error("After suite hook failed with error.", e);
		}
	}

	/**
	 * This is to override the default behaviour of shuffling the scenarios before executing them. Shuffling is done to ensure even mix of
	 * features is
	 * run in parallel to minimize/avoid bottlenecks do to simultaneous executions of similar flows
	 *
	 * @return true by default
	 */
	protected boolean shuffleScenarios() {
		return true;
	}

	@Override
	public ConfigurableApplicationContext getApplicationContext() {
		return (ConfigurableApplicationContext) this.applicationContext;
	}

	@Override
	public void beforeTestClass() throws Exception {
		super.springTestContextBeforeTestClass();
	}

	@Override
	public void afterTestClass() throws Exception {
		super.springTestContextAfterTestClass();
	}

}
