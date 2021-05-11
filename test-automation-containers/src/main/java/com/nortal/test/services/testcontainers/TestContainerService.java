package com.nortal.test.services.testcontainers;

import com.github.dockerjava.api.model.Container;
import com.nortal.test.services.testcontainers.images.builder.ReusableImageFromDockerfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.rules.ExternalResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.CustomFixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.ResourceReaper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This service is responsible for initializing and maintaining the black box testing setup.
 *
 */
@Component
@Slf4j
public class TestContainerService {

  private static final int INTERNAL_HTTP_PORT = 8080;
  private static final Duration TIMEOUT = Duration.ofSeconds(90);
  private static final String REUSABLE_NETWORK_NAME = "REUSABLE";

  private final List<ContextContainer> contextContainers;
//  private final ScenarioLogManager logManager;
  private final boolean reuseContainers;
  private Network network;

  public TestContainerService(
      final List<ContextContainer> contextContainers,
//      final ScenarioLogManager logManager,
      @Value("${context.reuse-containers:false}") final boolean reuseContainers
  ) {
    this.contextContainers = contextContainers;
//    this.logManager = logManager;
    this.reuseContainers = reuseContainers;
  }

  /**
   * Runs containers of context applications in parallel, also sets up db schema and redis.
   */
  public void startContext() {
    if (reuseContainers) {
      final Optional<com.github.dockerjava.api.model.Network> reusableNetworkOptional = findExistingReusableNetwork();
      if (reusableNetworkOptional.isEmpty()) {
        network = createReusableNetwork();
      } else {
        network = new ReusedNetwork(reusableNetworkOptional.get().getId());
      }
    } else {
      network = Network.newNetwork();
    }

    log.info("Starting context containers");
    final StopWatch timer = StopWatch.createStarted();
    contextContainers.parallelStream().forEach(it -> it.start(network));
    log.info("Context containers started in {}ms", timer.getTime());
  }

  private Network.NetworkImpl createReusableNetwork() {
    return Network
        .builder()
        .createNetworkCmdModifier(createNetworkCmd -> createNetworkCmd.withName(REUSABLE_NETWORK_NAME))
        .build();
  }

  private Optional<com.github.dockerjava.api.model.Network> findExistingReusableNetwork() {
    final List<com.github.dockerjava.api.model.Network> networks = DockerClientFactory.instance()
        .client()
        .listNetworksCmd()
        .exec();
    return networks
        .stream()
        .filter(network -> network.getName().equals(REUSABLE_NETWORK_NAME))
        .findAny();
  }

  /**
   * Starts the provided application image in a container.
   * Note: server.port must be set to 8080 for the correct port to be exposed.
   * @param image of the application under test
   */
  public void startApplicationUnderTest(final ReusableImageFromDockerfile image) {
    startApplicationUnderTest(image, TIMEOUT);
  }

  /**
   * Starts the provided application image in a container.
   * Note: server.port must be set to 8080 for the correct port to be exposed.
   * @param image of the application under test
   * @param timeout of the application under test
   */
  @SuppressWarnings("PMD")
  public void startApplicationUnderTest(final ReusableImageFromDockerfile image, final Duration timeout) {
    final GenericContainer applicationContainer = new GenericContainer(image)
        .withNetwork(network)
        .withExposedPorts(INTERNAL_HTTP_PORT)
        .withStartupTimeout(timeout);

    stopContainersOfOlderImage(image);
    startContainer(applicationContainer);
  }

  @SuppressWarnings("PMD.CloseResource")
  public void startApplicationUnderTest(final ReusableImageFromDockerfile image,
          final int[] fixedExposedPort,
          final Map<String,String> envConfig) {
    final CustomFixedHostPortGenericContainer customFixedHostPortGenericContainer =
        new CustomFixedHostPortGenericContainer(image);

    final List<Integer> allExposedPorts = new ArrayList<>();
    allExposedPorts.add(INTERNAL_HTTP_PORT);
    for (final int fixedPort : fixedExposedPort) {
      customFixedHostPortGenericContainer.withFixedExposedPort(fixedPort, fixedPort);
      allExposedPorts.add(fixedPort);
    }

    final GenericContainer applicationContainer =
        customFixedHostPortGenericContainer
            .withNetwork(network)
            .withExposedPorts(allExposedPorts.toArray(new Integer[0]))
                .withEnv(envConfig)
            .withStartupTimeout(TIMEOUT);

    stopContainersOfOlderImage(image);
    startContainer(applicationContainer);
  }

  private void stopContainersOfOlderImage(final ReusableImageFromDockerfile image) {
    final String imageNameWithVersion = image.getDockerImageName();
    if (imageNameWithVersion.contains(":")) {
      final String[] split = imageNameWithVersion.split(":");

      final String imageName = split[0];
      final String version = split[1];

      final List<Container> containersRunning = DockerClientFactory.instance()
          .client()
          .listContainersCmd()
          .exec();

      containersRunning
          .stream()
          .filter(container -> container.getImage().contains(imageName))
          .filter(container -> !container.getImage().contains(version))
          .forEach(containerToKill -> DockerClientFactory.instance()
              .client()
              .stopContainerCmd(containerToKill.getId())
              .exec()
          );
    }
  }

  /**
   * Starts all provided containers in parallel and registers log consumers for them.
   * @param containers containers to start under test
   */
  public void startApplicationsUnderTest(final Map<String, GenericContainer> containers) {
    log.info("Starting applications");
    final StopWatch timer = StopWatch.createStarted();

    containers.values().parallelStream()
        .map(it -> reuseContainers
                   ? it.withNetwork(network).withReuse(true)
                   : it.withNetwork(network))
        .forEach(GenericContainer::start);

  //TODO  containers.forEach(logManager::registerContainer);

    log.info("Applications started in {} ms", timer.getTime());
  }

  private void startContainer(final GenericContainer applicationContainer) {
    log.info("Starting application container");
    final StopWatch timer = StopWatch.createStarted();
    if (reuseContainers) {
      ContainerUtils.overrideNetworkAliases(applicationContainer, List.of());
      applicationContainer.withReuse(true).start();
    } else {
      applicationContainer.start();
    }
    log.info("Application container started in {}ms", timer.getTime());

    final Integer mappedPort = applicationContainer.getMappedPort(INTERNAL_HTTP_PORT);
    log.info("Mapping the exposed internal application port of {} to {}, setting RestAssured to default to it",
        INTERNAL_HTTP_PORT, mappedPort
    );
    System.setProperty("api.port", String.valueOf(mappedPort));
//    RestAssured.port = mappedPort;

    //TODO logManager.registerContainer("application under test", applicationContainer);
  }

  @RequiredArgsConstructor
  private static class ReusedNetwork extends ExternalResource implements Network {
    private final String id;

    @Override
    public String getId() {
      return id;
    }

    @Override
    public void close() {
      ResourceReaper.instance().removeNetworkById(id);
    }
  }

}
