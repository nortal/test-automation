/**
 * Copyright (c) 2022 Nortal AS
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.cucumber.junit.platform.engine;

import com.nortal.test.core.cucumber.TestReportProvider;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.feature.FeatureWithLines;
import io.cucumber.core.feature.GluePath;
import io.cucumber.core.options.ObjectFactoryParser;
import io.cucumber.core.options.PluginOption;
import io.cucumber.core.options.SnippetTypeParser;
import io.cucumber.core.options.UuidGeneratorParser;
import io.cucumber.core.plugin.NoPublishFormatter;
import io.cucumber.core.plugin.PublishFormatter;
import io.cucumber.core.snippets.SnippetType;
import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionParser;
import org.junit.platform.engine.ConfigurationParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.cucumber.core.resource.ClasspathSupport.CLASSPATH_SCHEME_PREFIX;
import static io.cucumber.junit.platform.engine.Constants.*;

class CucumberEngineOptions implements
        io.cucumber.core.plugin.Options,
        io.cucumber.core.runner.Options,
        io.cucumber.core.backend.Options {
    private static final Logger log = LoggerFactory.getLogger(CucumberEngineOptions.class);

    private final ConfigurationParameters configurationParameters;

    CucumberEngineOptions(ConfigurationParameters configurationParameters) {
        this.configurationParameters = configurationParameters;
    }

    @Override
    public List<Plugin> plugins() {
        final List<String> pluginNames = configurationParameters.get(PLUGIN_PROPERTY_NAME)
                .map(s -> Arrays.asList(s.split(",")))
                .orElseGet(ArrayList::new);

        getReportProvider().forEach(testReportProvider -> {
            log.info("Using {} as reporting plugin.", testReportProvider.getCucumberPlugin());
            pluginNames.add(testReportProvider.getCucumberPlugin());
        });

        List<Plugin> plugins = pluginNames.stream()
                .map(String::trim)
                .map(PluginOption::parse)
                .map(Plugin.class::cast)
                .collect(Collectors.toList());

        getPublishPlugin()
                .ifPresent(plugins::add);

        return plugins;
    }

    private List<TestReportProvider> getReportProvider() {
        ServiceLoader<TestReportProvider> loader = ServiceLoader.load(TestReportProvider.class);

        return StreamSupport
                .stream(loader.spliterator(), false)
                .collect(Collectors.toList());
    }

    private Optional<PluginOption> getPublishPlugin() {
        Optional<PluginOption> fromToken = getPublishTokenPlugin();
        Optional<PluginOption> fromEnabled = getPublishEnabledPlugin();

        Optional<PluginOption> plugin = Stream.of(fromToken, fromEnabled)
                .flatMap(pluginOption -> pluginOption.map(Stream::of).orElseGet(Stream::empty))
                .findFirst();

        // With higher java version use ifPresentOrElse in plugins()
        if (plugin.isPresent()) {
            return plugin;
        }
        return getPublishQuitePlugin();
    }

    private Optional<PluginOption> getPublishQuitePlugin() {
        Optional<PluginOption> noPublishOption = Optional.of(PluginOption.forClass(NoPublishFormatter.class));
        Optional<PluginOption> quiteOption = Optional.empty();
        return configurationParameters
                .getBoolean(PLUGIN_PUBLISH_QUIET_PROPERTY_NAME)
                .map(quite -> quite ? quiteOption : noPublishOption)
                .orElse(noPublishOption);
    }

    private Optional<PluginOption> getPublishTokenPlugin() {
        return configurationParameters
                .get(PLUGIN_PUBLISH_TOKEN_PROPERTY_NAME)
                .map(token -> PluginOption.forClass(PublishFormatter.class, token));
    }

    private Optional<PluginOption> getPublishEnabledPlugin() {
        Optional<PluginOption> enabledOption = Optional.of(PluginOption.forClass(PublishFormatter.class));
        Optional<PluginOption> disabledOption = Optional.empty();
        return configurationParameters
                .getBoolean(PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME)
                .flatMap(enabled -> enabled ? enabledOption : disabledOption);
    }

    @Override
    public boolean isMonochrome() {
        return configurationParameters
                .getBoolean(ANSI_COLORS_DISABLED_PROPERTY_NAME)
                .orElse(false);
    }

    @Override
    public boolean isWip() {
        return false;
    }

    Optional<Expression> tagFilter() {
        return configurationParameters.get(FILTER_TAGS_PROPERTY_NAME, TagExpressionParser::parse);
    }

    Optional<Pattern> nameFilter() {
        return configurationParameters.get(FILTER_NAME_PROPERTY_NAME, Pattern::compile);
    }

    @Override
    public List<URI> getGlue() {
        return configurationParameters
                .get(GLUE_PROPERTY_NAME, s -> Arrays.asList(s.split(",")))
                .orElse(Collections.singletonList(CLASSPATH_SCHEME_PREFIX))
                .stream()
                .map(String::trim)
                .map(GluePath::parse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isDryRun() {
        return configurationParameters
                .getBoolean(EXECUTION_DRY_RUN_PROPERTY_NAME)
                .orElse(false);
    }

    @Override
    public SnippetType getSnippetType() {
        return configurationParameters
                .get(SNIPPET_TYPE_PROPERTY_NAME, SnippetTypeParser::parseSnippetType)
                .orElse(SnippetType.UNDERSCORE);
    }

    @Override
    public Class<? extends ObjectFactory> getObjectFactoryClass() {
        return configurationParameters
                .get(OBJECT_FACTORY_PROPERTY_NAME, ObjectFactoryParser::parseObjectFactory)
                .orElse(null);
    }

    @Override
    public Class<? extends UuidGenerator> getUuidGeneratorClass() {
        return configurationParameters
                .get(UUID_GENERATOR_PROPERTY_NAME, UuidGeneratorParser::parseUuidGenerator)
                .orElse(null);
    }

    boolean isParallelExecutionEnabled() {
        return configurationParameters
                .getBoolean(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME)
                .orElse(false);
    }

    NamingStrategy namingStrategy() {
        return configurationParameters
                .get(JUNIT_PLATFORM_NAMING_STRATEGY_PROPERTY_NAME, DefaultNamingStrategy::getStrategy)
                .orElse(DefaultNamingStrategy.SHORT);
    }

    List<FeatureWithLines> featuresWithLines() {
        return configurationParameters.get(FEATURES_PROPERTY_NAME,
                        s -> Arrays.stream(s.split(","))
                                .map(String::trim)
                                .map(FeatureWithLines::parse)
                                .sorted(Comparator.comparing(FeatureWithLines::uri))
                                .distinct()
                                .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}
