package com.nortal.test.core.rest.interceptors;

import java.io.IOException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import com.nortal.test.core.services.ScenarioContainer;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@Component
public class HeaderInterceptor implements Interceptor {
	private final ScenarioContainer scenarioContainer;

	public HeaderInterceptor(@Lazy final ScenarioContainer scenarioContainer) {
		this.scenarioContainer = scenarioContainer;
	}

	@Override
	public Response intercept(final Interceptor.Chain chain) throws IOException {

		if (!scenarioContainer.get().getCommon().getHeaders().isEmpty()) {
			return setHeaderValues(chain);
		}

		return chain.proceed(chain.request());
	}

	private Response setHeaderValues(final Chain chain) throws IOException {
		final Request.Builder request = chain.request().newBuilder();
		scenarioContainer.get().getCommon().getHeaders().forEach(request::header);

		return chain.proceed(request.build());
	}
}