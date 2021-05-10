package com.nortal.test.postman.generate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nortal.test.postman.util.PostmanJsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import com.nortal.test.postman.PostmanScenarioRequestContext;
import com.nortal.test.postman.api.model.Item;
import com.nortal.test.postman.model.PostmanFeatureInfo;
import com.nortal.test.postman.util.PostmanScenarioNameUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;

@RequiredArgsConstructor
@Component
@Slf4j
public class PostmanDataCollector implements InitializingBean {
	private final OkHttpRequestRecorder okHttpRequestRecorder;

	@Getter
	private final Map<String, String> featureDescriptionMap = new ConcurrentHashMap<>();

	private final Map<PostmanFeatureInfo, Map<String, List<String>>> capturedRequestIds = new HashMap<>();

	private Map<String, String> requestByIdMap;

	private DB requestByIdDb;

	@Override
	public void afterPropertiesSet() {
		requestByIdDb = DBMaker
				.tempFileDB()
				.closeOnJvmShutdown()
				.fileDeleteAfterClose()
				.fileMmapEnableIfSupported()
				.make();
		requestByIdMap = requestByIdDb
				.hashMap("requestByIdMap", Serializer.STRING, Serializer.STRING)
				.createOrOpen();
	}

	public void record(final Response response,
	                   final PostmanScenarioRequestContext requestContext,
	                   final PostmanFeatureInfo featureInfo, final String scenarioId,
	                   final String scenarioName) {
		final String scenarioKey = PostmanScenarioNameUtils.createKey(scenarioId, scenarioName);
		if (featureInfo == null) {
			log.warn("Could not record request due to missing feature info for scenario: " + scenarioKey);
			return;
		}

		featureDescriptionMap.putIfAbsent(scenarioKey, featureInfo.getFeatureTitle());

		List<String> scenarioStepIds = getScenarioStepIds(featureInfo, scenarioKey);

		final Item requestItem = okHttpRequestRecorder.record(requestContext, response.request(), response, scenarioStepIds.size());

		scenarioStepIds.add(requestItem.getId());
		captureRequest(requestItem);
	}

	private synchronized List<String> getScenarioStepIds(final PostmanFeatureInfo featureInfo, final String scenarioKey) {
		Map<String, List<String>> scenarios = capturedRequestIds.computeIfAbsent(featureInfo, postmanFeatureInfo -> new ConcurrentHashMap<>());

		return scenarios.computeIfAbsent(scenarioKey, s -> new ArrayList<>());
	}

	Map<PostmanFeatureInfo, Map<String, List<String>>> getCapturedRequestIds() {
		return capturedRequestIds;
	}

	private synchronized void captureRequest(final Item requestItem) {
		requestByIdMap.put(requestItem.getId(), PostmanJsonUtils.serialize(requestItem));
	}

	Item getCapturedRequest(final String requestId) {
		try {
			final String requestString = requestByIdMap.get(requestId);
			return PostmanJsonUtils.deserialize(requestString, Item.class);
		} catch (final Exception exception) {
			log.warn("Could not get captured request: " + requestId, exception);
			return null;
		}
	}

	public void clear() {
		featureDescriptionMap.clear();
		capturedRequestIds.clear();
		requestByIdMap.clear();
	}
}
