package com.nortal.test.core.rest.interceptors;

import java.io.IOException;
import org.springframework.stereotype.Component;
import com.nortal.test.core.services.report.cucumber.RequestResponseReportFormatter;
import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.Response;

@Component
@RequiredArgsConstructor
public class ReportInterceptor implements Interceptor {

	private final RequestResponseReportFormatter formatter;

	@Override
	public Response intercept(final Interceptor.Chain chain) throws IOException {

		try {
			final Response response = chain.proceed(chain.request());
			formatter.formatAndAddToReport(response.request(), response);
			return response;
		} catch (Exception e) {
			formatter.formatAndAddToReport(chain.request());
			throw e;
		}
	}

}
