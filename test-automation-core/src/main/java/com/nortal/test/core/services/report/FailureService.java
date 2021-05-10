package com.nortal.test.core.services.report;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nortal.test.core.services.report.html.ReportErrorMessage;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Failing is the new passing.
 * <p>
 * This service is responsible for tracking scenario failures and exposing them to report generation.
 */
@Slf4j
@Component
public class FailureService {

	private Map<EnvironmentFailure, Long> failureCount = new EnumMap<>(EnvironmentFailure.class);

	public void reportEnvironmentFailure(EnvironmentFailure type) {
		failureCount.compute(type, (k, v) -> v == null ? 1L : v + 1L);
	}

	public List<ReportErrorMessage> getEnvironmentFailures() {
		return failureCount.entrySet()
				.stream()
				.filter(this::filterBelowThreshold)
				.map(entry -> new ReportErrorMessage(entry.getKey().getError(), entry.getKey().getDescription(), entry.getValue()))
				.collect(Collectors.toList());
	}

	private boolean filterBelowThreshold(Map.Entry<EnvironmentFailure, Long> entry) {
		final Long count = entry.getValue();
		final EnvironmentFailure failure = entry.getKey();
		final boolean include = count >= failure.getThreshold();
		log.info("Detected possible environment issue {} count is {}, threshold is {}, included in report [{}]", failure, count,
				failure.getThreshold(), include);
		return include;
	}

	public enum EnvironmentFailure {
		REDIS("Failure to retrieve account information from redis",
				"Account details are asynchronously populated by cortex. Them missing suggests cortex or upstream system issues.",
				3),
		GATEWAY_502("Gateway responds with 502",
				"This error means our gateway received an invalid response from an upstream API which suggests it is not in a healthy state.",
				2),
		SERVER_500("APIs respond with 500 error",
				   "This error means execution failed on the server side. A large amount of such errors signal a deeper underlying issue with the "
						   + "application.",
				   15),
		SERVICE_UNAVAILABLE_503("Service is unavailable to process a request",
								"The server cannot handle the request (because it is overloaded or down).",
								3),
		TIMEOUT("API calls are timing out",
				"Timeouts mean that whatever is at the other end is not responding. This suggests that the application is down.",
				2),
		INTERRUPTED_IO("API calls are getting interrupted",
				"This error indicates that an input or output transfer has been terminated because the thread performing it was interrupted",
				20),
		EIP_FLIP("Skus with EIP intent are flipping to FRP",
				 "A lot of flipping selectedPricingOption suggests possible issues with EIP feed import.",
				 10),
		GOLDEN_DATA_LOCK("Golden Data lock is missing",
						 "Test suite failed to initially acquire a golden data lock, or it was removed during polling.",
						 1),
		MISSING_GOLDEN_DATA("Sku is not present in GD data",
							"Scenario attempted to use a SKU that is not present in the golden data set.",
							1),
		MISSING_SKU_IN_SOLR("Sku is missing in SOLR",
							"Scenario attempted to use a SKU that is missing in SOLR.",
							10);

		EnvironmentFailure(final String error, final String description, final int threshold) {
			this.error = error;
			this.description = description;
			this.threshold = threshold;
		}

		private String error;
		private String description;
		private int threshold; //how many failures it take to appear on report

		public String getError() {
			return error;
		}

		public String getDescription() {
			return description;
		}

		public int getThreshold() {
			return threshold;
		}
	}
}
