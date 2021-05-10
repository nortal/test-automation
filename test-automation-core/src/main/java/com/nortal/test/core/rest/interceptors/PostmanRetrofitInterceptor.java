package com.nortal.test.core.rest.interceptors;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.nortal.test.core.plugin.CucumberScenarioNameProvider;
import com.nortal.test.core.services.ScenarioContainer;
import com.nortal.test.postman.generate.PostmanDataCollector;
import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.Response;

@RequiredArgsConstructor
@Component
public class PostmanRetrofitInterceptor implements Interceptor {
	private static final ThreadLocal<Boolean> THREAD_LOCAL_SWITCH = new InheritableThreadLocal<>();

	private final PostmanDataCollector postmanDataCollector;
	private final ScenarioContainer scenarioContainer;

	@Value("${postman-generator.enabled:false}")
	private boolean enabled;

	@Override
	public Response intercept(final Chain chain) throws IOException {
		final Response response = chain.proceed(chain.request());

		if (isEnabled()) {
			postmanDataCollector.record(response,
			                            scenarioContainer.get(),
			                            CucumberScenarioNameProvider.getInstance().getFeatureInfo(),
			                            scenarioContainer.getScenario().getId(),
			                            scenarioContainer.getScenario().getName());
		}

		return response;
	}

	private boolean isEnabled() {
		return enabled && Boolean.TRUE.equals(THREAD_LOCAL_SWITCH.get());
	}

	public static void enable() {
		THREAD_LOCAL_SWITCH.set(Boolean.TRUE);
	}

	public static void disable() {
		THREAD_LOCAL_SWITCH.remove();
	}
}
