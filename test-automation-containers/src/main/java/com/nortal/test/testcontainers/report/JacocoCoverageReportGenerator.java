package com.nortal.test.testcontainers.report;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.nortal.test.core.services.hooks.BeforeSuiteHook;
import com.nortal.test.testcontainers.configuration.TestContainerProperties;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.jacoco.core.tools.ExecFileLoader;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;
import org.springframework.stereotype.Component;

@Component
public class JacocoCoverageReportGenerator implements BeforeSuiteHook {

	private static final String DESTDIR = "build/jacoco/";
	private static final String DESTFILE = DESTDIR + "system-tests.exec";
	private static final String DESTFILE_XML = "build/jacoco/system-tests.xml";
	private static final String ADDRESS = "localhost";

	private final TestContainerProperties testContainerProperties;

	public JacocoCoverageReportGenerator(TestContainerProperties testContainerProperties) {
		this.testContainerProperties = testContainerProperties;
	}

	@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
	public void generate() {
		try {
			transferExecutionData();
			generateReport();
		} catch (Exception e) {
			throw new RuntimeException("System tests coverage report generator failed", e);
		}
	}

	private void generateReport() throws IOException {
		final var execFileLoader = new ExecFileLoader();
		execFileLoader.load(Files.newInputStream(Paths.get(DESTFILE)));

		final IBundleCoverage bundleCoverage = analyzeStructure(execFileLoader);

		createXmlReport(bundleCoverage, execFileLoader);
		createHtmlReport(bundleCoverage, execFileLoader);
	}

	private IBundleCoverage analyzeStructure(final ExecFileLoader execFileLoader) throws IOException {
		final var coverageBuilder = new CoverageBuilder();
		final var analyzer = new Analyzer(
				execFileLoader.getExecutionDataStore(), coverageBuilder);

		try (var stream = Files.find(Paths.get(".."), 10,
				(path, basicFileAttributes) -> path.toString().matches(".+build.classes.+[\\\\,/]main"))) {
			stream.forEach(path -> {
				try {
					analyzer.analyzeAll(path.toFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}

		return coverageBuilder.getBundle("System tests coverage report");
	}

	private void createXmlReport(final IBundleCoverage bundleCoverage, final ExecFileLoader execFileLoader)
			throws IOException {
		final var xmlFormatter = new XMLFormatter();

		var localFile = Files.newOutputStream(Paths.get(DESTFILE_XML));
		var visitor = xmlFormatter.createVisitor(localFile);

		visitResults(bundleCoverage, execFileLoader, visitor);
	}

	private void createHtmlReport(final IBundleCoverage bundleCoverage, final ExecFileLoader execFileLoader) throws IOException {
		final var htmlFormatter = new HTMLFormatter();
		final var visitor = htmlFormatter
				.createVisitor(new FileMultiReportOutput(new File("build/reports/system-tests")));

		visitResults(bundleCoverage, execFileLoader, visitor);
	}

	private void visitResults(final IBundleCoverage bundleCoverage, final ExecFileLoader execFileLoader, final IReportVisitor visitor)
			throws IOException {
		visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
				execFileLoader.getExecutionDataStore().getContents()
		);

		try (var stream = Files.find(Paths.get(".."), 10,
				(path, basicFileAttributes) -> path.toString().matches(".+src[\\\\,/]main[\\\\,/](kotlin|java)"))) {
			stream.forEach(path -> {
				try {
					// Populate the report structure with the bundle coverage information.
					// Call visitGroup if you need groups in your report.
					visitor.visitBundle(bundleCoverage,
							new DirectorySourceFileLocator(path.toFile(), "utf-8", 2)
					);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
		visitor.visitEnd();
	}

	private void transferExecutionData() throws IOException {
		createTargetDir();

		try (var socket = new Socket(InetAddress.getByName(ADDRESS), testContainerProperties.getTestableContainer().getJacoco().getPort());
			 var localFile = Files.newOutputStream(Paths.get(DESTFILE))) {
			final var localWriter = new ExecutionDataWriter(localFile);

			final var writer = new RemoteControlWriter(socket.getOutputStream());
			final var reader = new RemoteControlReader(socket.getInputStream());
			reader.setSessionInfoVisitor(localWriter);
			reader.setExecutionDataVisitor(localWriter);

			// Send a dump command and read the response:
			writer.visitDumpCommand(true, false);
			if (!reader.read()) {
				throw new IOException("Socket closed unexpectedly.");
			}
		}
	}

	private void createTargetDir() {
		var targetDir = new File(DESTDIR);
		if (!targetDir.exists()) {
			targetDir.mkdir();
		}
	}

	@Override
	public void beforeSuite() {
		if (testContainerProperties.getTestableContainer().getJacoco().getEnabled()) {
			Runtime.getRuntime().addShutdownHook(new Thread(this::generate));
		}
	}
}

