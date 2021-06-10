package com.nortal.test.postman.generate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.nortal.test.postman.api.model.Event;
import com.nortal.test.postman.api.model.EventScript;
import com.nortal.test.postman.api.model.Item;
import com.nortal.test.postman.model.PostmanExecutionContext;
import com.nortal.test.postman.util.ClassPathReader;
import com.nortal.test.postman.util.PostmanVariableUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
@Component
public class PostmanScriptInjector implements InitializingBean {
	private static final Set<String> AUTH_HOSTS = ImmutableSet.of(PostmanVariableUtils.asVariableKey("jwt"));
	private LoadingCache<String, String> scriptCache;
	private final ClassPathReader classPathReader;

	public List<Item> injectScripts(final PostmanExecutionContext context, final List<Item> items) {
		items.forEach(item -> injectScripts(context, item));

		return items;
	}

	public void injectScripts(final PostmanExecutionContext context, final Item item) {
		injectAuthScriptIfNecessary(item);
		injectCartScriptIfNecessary(item);
		injectCartLineScriptIfNecessary(item);
	}

	private void injectAuthScriptIfNecessary(final Item item) {
		if (item.getRequest() != null && AUTH_HOSTS.contains(item.getRequest().getUrl().getHost())) {
			Event event = createScriptEvent("set-authentication.js");
			item.setEvents(Collections.singletonList(event));
		}
	}

	private void injectCartScriptIfNecessary(final Item item) {
		if (item.getRequest().getUrl().getRaw().endsWith("/commerce/v3/carts/")) {
			Event event = createScriptEvent("set-cart-id.js");
			item.setEvents(Collections.singletonList(event));
		}
	}

	private void injectCartLineScriptIfNecessary(final Item item) {
		if (item.getRequest().getUrl().getRaw().endsWith("/lines")) {
			Event event = createScriptEvent("set-line-id.js");
			item.setEvents(Collections.singletonList(event));
		}
	}

	@SneakyThrows
	public synchronized String getScript(final String scriptFile) {
		return scriptCache.get(scriptFile);
	}

	private Event createScriptEvent(final String scriptFile) {
		final String script = getScript(scriptFile);

		return Event.builder()
				.disabled(false)
				.listen("test")
				.script(EventScript.builder()
						        .id(UUID.randomUUID().toString())
						        .exec(Arrays.asList(script.split("\\r")))
						        .type("application/json")
						        .build())
				.build();
	}

	@SneakyThrows
	private String getScriptFromClassPath(final String scriptFile) {
		return new String(classPathReader.readResource("classpath:scripts/" + scriptFile).getInputStream().readAllBytes());
	}

	@Override
	public void afterPropertiesSet() {
		CacheLoader<String, String> loader;
		loader = new CacheLoader<>() {
			@Override
			public String load(String scriptFile) {
				return getScriptFromClassPath(scriptFile);
			}
		};

		scriptCache = CacheBuilder.newBuilder().build(loader);
	}
}
