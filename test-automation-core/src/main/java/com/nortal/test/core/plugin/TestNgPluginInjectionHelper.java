package com.nortal.test.core.plugin;

import com.nortal.test.core.exceptions.TestExecutionException;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.plugin.Plugin;
import io.cucumber.testng.TestNGCucumberRunner;

import java.lang.reflect.Field;

/**
 * Since TestNg does not give us a way to add our own cucumber plugin instances we have to resort to unspeakable things done here.
 * (the only other way to register plugins is via cucumber options which gives us no way to manage the plugin lifecycle ourselves e.g. as a spring bean)
 */
public class TestNgPluginInjectionHelper {

	public static void inject(TestNGCucumberRunner runner, Plugin plugin) {
		try {
			final Field pluginsField = TestNGCucumberRunner.class.getDeclaredField("plugins");
			pluginsField.setAccessible(true);
			final Plugins plugins = (Plugins) pluginsField.get(runner);
			plugins.addPlugin(plugin);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new TestExecutionException("Failed to inject cucumber plugin into TestNgCucumberRunner");
		}
	}
}
