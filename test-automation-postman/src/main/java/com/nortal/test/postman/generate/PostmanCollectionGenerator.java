package com.nortal.test.postman.generate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.nortal.test.postman.model.PostmanExecutionContext;
import com.nortal.test.postman.model.PostmanFeatureInfo;
import com.nortal.test.postman.util.PostmanCollectionFileNameUtils;
import com.nortal.test.postman.util.PostmanJsonUtils;
import com.nortal.test.postman.util.PostmanScenarioNameUtils;
import com.nortal.test.postman.util.ReportFileUtils;
import org.springframework.stereotype.Component;
import com.nortal.test.postman.api.model.CollectionVersion;
import com.nortal.test.postman.api.model.Information;
import com.nortal.test.postman.api.model.Item;
import com.nortal.test.postman.api.model.PostmanCollection;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class PostmanCollectionGenerator {
	private static final String SCHEMA = "https://schema.getpostman.com/json/collection/v2.1.0/collection.json";
	public static final String COLLECTION_PATH = "%s/cucumber-html-reports/postman-collection/";

	private final PostmanScriptInjector postmanScriptInjector;
	private final PostmanDataCollector postmanDataCollector;

	@SneakyThrows
	public void generate(final PostmanExecutionContext context) {
		CollectionVersion version = CollectionVersion.builder()
				.identifier("tmo")
				.major(2)
				.minor(1)
				.build();

		final String outputDir = String.format(COLLECTION_PATH, context.getOutputDir());
		ReportFileUtils.createDirectory(outputDir);

		final Map<PostmanFeatureInfo, Map<String, List<String>>> requests = postmanDataCollector.getCapturedRequestIds();
		final List<Item> features = createFeatures(context, requests);
		for (Item feature : features) {
			generateCollection(context, version, outputDir, feature);
		}

		postmanDataCollector.clear();
	}

	private List<Item> createFeatures(final PostmanExecutionContext context, final Map<PostmanFeatureInfo, Map<String, List<String>>> requests) {
		final List<Item> features = new ArrayList<>();

		final Iterator<Map.Entry<PostmanFeatureInfo, Map<String, List<String>>>> featureInfoIterator = requests.entrySet().iterator();
		while (featureInfoIterator.hasNext()) {
			final Map.Entry<PostmanFeatureInfo, Map<String, List<String>>> entry = featureInfoIterator.next();
			final Map<String, List<Item>> featureScenarios = collectFeatureScenarios(entry.getValue());

			final Item feature = Item.builder()
					.id(entry.getKey().getFeatureId())
					.name(entry.getKey().getFeatureTitle())
					.description(entry.getKey().getFormattedFeatureDescription())
					.items(createScenario(context, featureScenarios))
					.build();

			features.add(feature);
			context.getCollectionNames().put(feature.getId(), feature.getName());

			featureInfoIterator.remove();
		}
		return features;
	}

	private Map<String, List<Item>> collectFeatureScenarios(final Map<String, List<String>> requestMap) {
		return requestMap.keySet().stream()
				.collect(Collectors.toMap(key -> key, key -> requestMap.getOrDefault(key, Collections.emptyList()).stream()
						.map(postmanDataCollector::getCapturedRequest)
						.filter(Objects::nonNull)
						.collect(Collectors.toList())));
	}

	private List<Item> createScenario(final PostmanExecutionContext context, final Map<String, List<Item>> featureScenarios) {
		final List<Item> scenarios = new ArrayList<>();

		featureScenarios.forEach((scenarioKey, items) -> {
			Item scenario = Item.builder()
					.name(PostmanScenarioNameUtils.getScenarioName(scenarioKey))
					.items(postmanScriptInjector.injectScripts(context, items))
					.build();

			scenarios.add(scenario);
		});

		return scenarios;
	}

	private void generateCollection(final PostmanExecutionContext context, final CollectionVersion version, final String outputDir,
									final Item feature) {
		final String collectionName = context.getExecutionTitle() + " " + feature.getName();
		final PostmanCollection def = PostmanCollection.builder()
				.items(Collections.singletonList(feature))
				.info(Information.builder()
						.postmanID("1")
						.name(collectionName)
						.schema(SCHEMA)
						.version(version)
						.build())
				.build();
		final String fileName = PostmanCollectionFileNameUtils.createFileName(feature.getId());
		PostmanJsonUtils.serializeToFile(fileName, outputDir, def);
	}
}

