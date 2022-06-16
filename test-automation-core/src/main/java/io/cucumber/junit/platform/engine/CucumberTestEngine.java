/**
 * Copyright (c) 2022 Nortal AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.cucumber.junit.platform.engine;

import static io.cucumber.junit.platform.engine.Constants.PARALLEL_CONFIG_PREFIX;
import static io.cucumber.junit.platform.engine.Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME;

import com.nortal.test.core.property.JUnitPropertyInitializer;
import org.apiguardian.api.API;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.config.PrefixedConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService;

/**
 * <b>NOTE: It's a copy of existing class with additinal {@link  JUnitPropertyInitializer} execution.</b>
 * <p>
 * The Cucumber {@link org.junit.platform.engine.TestEngine TestEngine}.
 * <p>
 * Supports discovery and execution of {@code .feature} files using the
 * following selectors:
 * <ul>
 * <li>{@link org.junit.platform.engine.discovery.ClasspathRootSelector}</li>
 * <li>{@link org.junit.platform.engine.discovery.ClasspathResourceSelector}</li>
 * <li>{@link org.junit.platform.engine.discovery.PackageSelector}</li>
 * <li>{@link org.junit.platform.engine.discovery.FileSelector}</li>
 * <li>{@link org.junit.platform.engine.discovery.DirectorySelector}</li>
 * <li>{@link org.junit.platform.engine.discovery.UniqueIdSelector}</li>
 * <li>{@link org.junit.platform.engine.discovery.UriSelector}</li>
 * </ul>
 */
@API(status = API.Status.STABLE)
public final class CucumberTestEngine extends HierarchicalTestEngine<CucumberEngineExecutionContext> {

	public CucumberTestEngine() {
		JUnitPropertyInitializer.initializeProperties();
	}

	@Override
	public String getId() {
		return "cucumber";
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
		CucumberEngineDescriptor engineDescriptor = new CucumberEngineDescriptor(uniqueId);
		new DiscoverySelectorResolver().resolveSelectors(discoveryRequest, engineDescriptor);
		return engineDescriptor;
	}

	@Override
	protected HierarchicalTestExecutorService createExecutorService(ExecutionRequest request) {
		ConfigurationParameters config = request.getConfigurationParameters();
		if (config.getBoolean(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME).orElse(false)) {
			return new ForkJoinPoolHierarchicalTestExecutorService(
					new PrefixedConfigurationParameters(config, PARALLEL_CONFIG_PREFIX));
		}
		return super.createExecutorService(request);
	}

	@Override
	protected CucumberEngineExecutionContext createExecutionContext(ExecutionRequest request) {
		return new CucumberEngineExecutionContext(request.getConfigurationParameters());
	}

}
