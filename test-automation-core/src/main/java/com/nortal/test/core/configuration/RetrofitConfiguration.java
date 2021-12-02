package com.nortal.test.core.configuration;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.nortal.test.core.rest.interceptors.HeaderInterceptor;
import com.nortal.test.core.rest.interceptors.LoggingInterceptor;
import com.nortal.test.core.rest.interceptors.ReportInterceptor;
import lombok.SneakyThrows;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetrofitConfiguration {
	private final ReportInterceptor reportFilter;
	private final LoggingInterceptor loggingFilter;
	private final HeaderInterceptor headerInterceptor;
	private final Integer httpTimeout;


	public RetrofitConfiguration(final ReportInterceptor reportFilter,
			final LoggingInterceptor loggingFilter,
			final HeaderInterceptor headerInterceptor,
			@Value("${test-automation.integration.http.connection-time-out:30}") final Integer httpTimeout) {
		this.reportFilter = reportFilter;
		this.loggingFilter = loggingFilter;
		this.headerInterceptor = headerInterceptor;
		this.httpTimeout = httpTimeout;
	}

	@Bean
	@Qualifier("testHttpClient")
	public OkHttpClient createRetrofitApiWithTMOInterceptors() {
		return httpClientWithInterceptors(asList(
				headerInterceptor,
				loggingFilter,
				reportFilter))
				.build();
	}


	private OkHttpClient.Builder httpClientWithInterceptors(List<Interceptor> interceptors) {
		final OkHttpClient.Builder builder = getPreBakedHttpClient();
		interceptors.forEach(builder::addInterceptor);
		return builder;
	}


	private OkHttpClient.Builder getPreBakedHttpClient() {
		final X509TrustManager trustManager = getTrustManager();
		return new OkHttpClient.Builder()
				.sslSocketFactory(getSSLSocketFactory(trustManager), trustManager)
				.hostnameVerifier((hostname, session) -> true)
				//I know these numbers are huge, but sometimes http calls like to take their sweet time responding with 10+ seconds of read being
				// not uncommon
				.readTimeout(Duration.ofSeconds(httpTimeout))
				.connectTimeout(Duration.ofSeconds(httpTimeout))
				.writeTimeout(Duration.ofSeconds(httpTimeout))
				.callTimeout(Duration.ofSeconds(httpTimeout));
	}

	@SneakyThrows
	private SSLSocketFactory getSSLSocketFactory(final X509TrustManager trustManager) {
		final SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
		return sslContext.getSocketFactory();
	}

	private X509TrustManager getTrustManager() {
		return new X509TrustManager() {
			@Override
			public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s) {
			}

			@Override
			public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s) {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		};
	}

	private static class FloatTypeAdapter extends TypeAdapter<Float> {
		@Override
		public Float read(JsonReader reader) throws IOException {
			if (reader.peek() == JsonToken.NULL) {
				reader.nextNull();
				return null;
			}
			String stringValue = reader.nextString();
			try {
				return Float.parseFloat(stringValue);
			} catch (NumberFormatException e) {
				return null;
			}
		}

		@Override
		public void write(JsonWriter writer, Float value) throws IOException {
			if (value == null) {
				writer.nullValue();
				return;
			}
			writer.value(value);
		}
	}

}
