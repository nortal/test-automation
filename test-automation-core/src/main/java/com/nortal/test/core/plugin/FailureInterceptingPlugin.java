package com.nortal.test.core.plugin;

import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.Optional;

import com.nortal.test.core.rest.error.RestCallException;
import org.springframework.stereotype.Component;
import com.nortal.test.core.exceptions.IntentFlipException;
import com.nortal.test.core.exceptions.MissingGoldenDataException;
import com.nortal.test.core.exceptions.MissingSkuInSolrException;
import com.nortal.test.core.exceptions.RedisException;
import com.nortal.test.core.services.report.FailureService;
import com.nortal.test.core.services.report.FailureService.EnvironmentFailure;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.TestCaseFinished;
import lombok.RequiredArgsConstructor;

/**
 * This plugin intercepts failing scenarios and reports their exception to the failure service depending if its a regular failure or an env failure
 */
@Component
@RequiredArgsConstructor
public class FailureInterceptingPlugin implements ConcurrentEventListener {

	private final FailureService failureService;

	@Override
	public void setEventPublisher(final EventPublisher publisher) {
		publisher.registerHandlerFor(TestCaseFinished.class, this::handleEvent);
	}

	private void handleEvent(final TestCaseFinished event) {
		final Result result = event.getResult();
		if (result.getStatus().isOk(true)) {
			return;
		}

		checkForEnvironmentFailures(result.getError())
				.ifPresent(failureService::reportEnvironmentFailure);
	}

	private Optional<EnvironmentFailure> checkForEnvironmentFailures(final Throwable error) {
		if (error instanceof RedisException) {
			return Optional.of(EnvironmentFailure.REDIS);
		} else if (error instanceof MissingGoldenDataException) {
			return Optional.of(EnvironmentFailure.MISSING_GOLDEN_DATA);
		} else if (error instanceof MissingSkuInSolrException) {
			return Optional.of(EnvironmentFailure.MISSING_SKU_IN_SOLR);
		} else if (error instanceof IntentFlipException) {
			return Optional.of(EnvironmentFailure.EIP_FLIP);
		} else if (error instanceof InterruptedIOException) {
			return Optional.of(EnvironmentFailure.INTERRUPTED_IO);
		} else if (error instanceof RestCallException) {
			final RestCallException restCallException = (RestCallException) error;
			if (restCallException.getCause() instanceof SocketTimeoutException) {
				return Optional.of(EnvironmentFailure.TIMEOUT);
			} else if (restCallException.getCode() == 502) {
				return Optional.of(EnvironmentFailure.GATEWAY_502);
			} else if (restCallException.getCode() == 503) {
				return Optional.of(EnvironmentFailure.SERVICE_UNAVAILABLE_503);
			} else if (restCallException.getCode() == 500) {
				return Optional.of(EnvironmentFailure.SERVER_500);
			}
		}
		return Optional.empty();
	}
}
