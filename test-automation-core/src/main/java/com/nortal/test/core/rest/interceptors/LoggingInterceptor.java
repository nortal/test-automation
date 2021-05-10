package com.nortal.test.core.rest.interceptors;

import java.io.IOException;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Response;

@Slf4j
@Component
public class LoggingInterceptor implements Interceptor {

	@Override
	public Response intercept(final Interceptor.Chain chain) throws IOException {
		var request = chain.request();

		var startTime = System.currentTimeMillis();

		var response = chain.proceed(request);

		var duration = System.currentTimeMillis() - startTime;
		log.info("Received response with code {} for {} in {}ms.", response.code(), response.request().url(), duration);

		return response;
	}

}
