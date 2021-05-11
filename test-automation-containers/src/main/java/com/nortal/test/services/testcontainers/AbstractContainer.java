package com.nortal.test.services.testcontainers;

import lombok.SneakyThrows;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import java.util.List;

public abstract class AbstractContainer<T extends GenericContainer<T>> implements ContextContainer {

  private final T container;
  private final boolean reuseContainers;

  public AbstractContainer(final T container, final boolean reuseContainers) {
    this.container = container;
    this.reuseContainers = reuseContainers;
  }

  @SneakyThrows
  @Override
  public void start(final Network network) {
    if (reuseContainers) {
      // required in order to avoid random aliases that makes container hash different and does not allow reuse
      ContainerUtils.overrideNetworkAliases(this.container, getAliases());
      container
          .withNetwork(network)
          .withReuse(true)
          .start();
    } else {
      container
          .withNetwork(network)
          .start();
    }
  }

  protected T getContainer() {
    return container;
  }

  protected abstract List<String> getAliases();

}
