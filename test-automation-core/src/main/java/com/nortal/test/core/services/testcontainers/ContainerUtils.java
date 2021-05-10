package com.nortal.test.core.services.testcontainers;

import java.util.List;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.testcontainers.containers.GenericContainer;

@UtilityClass
public class ContainerUtils {

  @SneakyThrows
  public static void overrideNetworkAliases(final GenericContainer<?> genericContainer, final List<String> aliases) {
    FieldUtils.writeField(genericContainer, "networkAliases", aliases, true);
  }

}
