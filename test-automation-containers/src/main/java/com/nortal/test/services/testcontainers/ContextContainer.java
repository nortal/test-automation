package com.nortal.test.services.testcontainers;

import org.testcontainers.containers.Network;

/**
 * Interface marking the container as a context one.
 * Context containers are initialized and started before the system under test.
 */
public interface ContextContainer {

  void start(Network network);

}
