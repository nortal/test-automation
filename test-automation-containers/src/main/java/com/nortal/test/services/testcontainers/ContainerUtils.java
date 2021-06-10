package com.nortal.test.services.testcontainers;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.testcontainers.containers.GenericContainer;

import java.util.List;

@UtilityClass
public class ContainerUtils {

  @SneakyThrows
  public static void overrideNetworkAliases(final GenericContainer<?> genericContainer, final List<String> aliases) {
    FieldUtils.writeField(genericContainer, "networkAliases", aliases, true);
  }

}
