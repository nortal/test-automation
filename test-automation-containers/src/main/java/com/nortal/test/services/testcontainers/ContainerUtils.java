package com.nortal.test.services.testcontainers;

import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.testcontainers.containers.GenericContainer;

public class ContainerUtils {


	public static void overrideNetworkAliases(final GenericContainer<?> genericContainer, final List<String> aliases) {
		try {
			FieldUtils.writeField(genericContainer, "networkAliases", aliases, true);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

}
