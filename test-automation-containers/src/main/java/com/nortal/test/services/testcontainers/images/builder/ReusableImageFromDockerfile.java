package com.nortal.test.services.testcontainers.images.builder;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;
import lombok.Cleanup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.images.ParsedDockerfile;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.images.builder.traits.BuildContextBuilderTrait;
import org.testcontainers.images.builder.traits.ClasspathTrait;
import org.testcontainers.images.builder.traits.DockerfileTrait;
import org.testcontainers.images.builder.traits.FilesTrait;
import org.testcontainers.images.builder.traits.StringsTrait;
import org.testcontainers.shaded.org.apache.commons.lang.StringUtils;
import org.testcontainers.utility.Base58;
import org.testcontainers.utility.DockerLoggerFactory;
import org.testcontainers.utility.LazyFuture;
import org.testcontainers.utility.ResourceReaper;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import static java.util.Optional.ofNullable;

/**
 * A class that is a plain copy of {@link org.testcontainers.images.builder.ImageFromDockerfile} + majority PMDs fixed.
 * Created just in order to be able to reuse containers that are created from images that are built using this class.
 * */
@SuppressWarnings("PMD.CloseResource")
@Slf4j
@Getter
public class ReusableImageFromDockerfile extends LazyFuture<String> implements
    BuildContextBuilderTrait<ReusableImageFromDockerfile>,
    ClasspathTrait<ReusableImageFromDockerfile>,
    FilesTrait<ReusableImageFromDockerfile>,
    StringsTrait<ReusableImageFromDockerfile>,
    DockerfileTrait<ReusableImageFromDockerfile> {

  private final String dockerImageName;
  private final Map<String, Transferable> transferables = new HashMap<>();
  private final Map<String, String> buildArgs = new HashMap<>();
  private boolean deleteOnExit;
  private boolean reusableContainer;
  private String dockerFilePath;
  private Path dockerfile;
  private Set<String> dependencyImageNames = Collections.emptySet();

  public ReusableImageFromDockerfile() {
    this("testcontainers/" + Base58.randomString(16).toLowerCase(Locale.getDefault()));
  }

  public ReusableImageFromDockerfile(final String dockerImageName) {
    this(dockerImageName, true, false);
  }

  public ReusableImageFromDockerfile(
      final String dockerImageName, final boolean deleteOnExit, final boolean reusableContainer
  ) {
    super();
    this.dockerImageName = dockerImageName;
    this.deleteOnExit = deleteOnExit;
    this.reusableContainer = reusableContainer;
  }

  @Override
  public ReusableImageFromDockerfile withFileFromTransferable(final String path, final Transferable transferable) {
    final Transferable oldValue = transferables.put(path, transferable);

    if (oldValue != null) {
      log.warn("overriding previous mapping for '{}'", path);
    }

    return this;
  }

  @Override
  protected final String resolve() {
    final Logger logger = DockerLoggerFactory.getLogger(dockerImageName);

    final DockerClient dockerClient = DockerClientFactory.instance().client();

    try {
      if (deleteOnExit) {
        ResourceReaper.instance().registerImageForCleanup(dockerImageName);
      }

      final BuildImageResultCallback resultCallback = new BuildImageResultCallback() {
        @Override
        public void onNext(final BuildResponseItem item) {
          super.onNext(item);

          if (item.isErrorIndicated()) {
            logger.error(item.getErrorDetail().getMessage());
          } else {
            logger.debug(StringUtils.chomp(item.getStream(), "\n"));
          }
        }
      };

      // We have to use pipes to avoid high memory consumption since users might want to build really big images
      @Cleanup final PipedInputStream in = new PipedInputStream();
      @Cleanup final PipedOutputStream out = new PipedOutputStream(in);

      final BuildImageCmd buildImageCmd = dockerClient.buildImageCmd(in);
      configure(buildImageCmd);
      final Map<String, String> labels = new HashMap<>();
      if (buildImageCmd.getLabels() != null) {
        labels.putAll(buildImageCmd.getLabels());
      }
      labels.putAll(
          this.reusableContainer
          ? removeSessionIdLabel(DockerClientFactory.DEFAULT_LABELS)
          : DockerClientFactory.DEFAULT_LABELS
      );
      buildImageCmd.withLabels(labels);

      prePullDependencyImages(dependencyImageNames);

      final BuildImageResultCallback exec = buildImageCmd.exec(resultCallback);

      long bytesToDockerDaemon = 0;

      // To build an image, we have to send the context to Docker in TAR archive format
      try (TarArchiveOutputStream tarArchive = new TarArchiveOutputStream(new GZIPOutputStream(out))) {
        tarArchive.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

        for (final Map.Entry<String, Transferable> entry : transferables.entrySet()) {
          final Transferable transferable = entry.getValue();
          final String destination = entry.getKey();
          transferable.transferTo(tarArchive, destination);
          bytesToDockerDaemon += transferable.getSize();
        }
        tarArchive.finish();
      }

      log.info("Transferred {} to Docker daemon", FileUtils.byteCountToDisplaySize(bytesToDockerDaemon));
      if (bytesToDockerDaemon > FileUtils.ONE_MB * 50) // warn if >50MB sent to docker daemon
      {
        log.warn(
            "A large amount of data was sent to the Docker daemon ({}). Consider using a .dockerignore file for better performance.",
            FileUtils.byteCountToDisplaySize(bytesToDockerDaemon)
        );
      }

      exec.awaitImageId();

      return dockerImageName;
    } catch (IOException e) {
      throw new IllegalStateException("Can't close DockerClient", e);
    }
  }

  private Map<String, String> removeSessionIdLabel(final Map<String, String> labels) {
    final Map<String, String> defaultLabels = new HashMap<>(labels);
    defaultLabels.remove(DockerClientFactory.TESTCONTAINERS_SESSION_ID_LABEL);
    return defaultLabels;
  }

  protected void configure(final BuildImageCmd buildImageCmd) {
    buildImageCmd.withTag(this.getDockerImageName());
    ofNullable(dockerFilePath).ifPresent(buildImageCmd::withDockerfilePath);
    ofNullable(dockerfile).ifPresent(p -> {
      buildImageCmd.withDockerfile(p.toFile());
      dependencyImageNames = new ParsedDockerfile(p).getDependencyImageNames();

      if (!dependencyImageNames.isEmpty()) {
        // if we'll be pre-pulling images, disable the built-in pull as it is not necessary and will fail for
        // authenticated registries
        buildImageCmd.withPull(false);
      }
    });

    this.buildArgs.forEach(buildImageCmd::withBuildArg);
  }

  private void prePullDependencyImages(final Set<String> imagesToPull) {
    final DockerClient dockerClient = DockerClientFactory.instance().client();

    imagesToPull.forEach(imageName -> {
      try {
        log.info(
            "Pre-emptively checking local images for '{}', referenced via a Dockerfile. If not available, it will be pulled.",
            imageName
        );
        DockerClientFactory.instance().checkAndPullImage(dockerClient, imageName);
      } catch (Exception e) {
        log.warn(
            "Unable to pre-fetch an image ({}) depended upon by Dockerfile - image build will continue but may fail. Exception message was: {}",
            imageName, e.getMessage()
        );
      }
    });
  }

  public ReusableImageFromDockerfile withBuildArg(final String key, final String value) {
    this.buildArgs.put(key, value);
    return this;
  }

  public ReusableImageFromDockerfile withBuildArgs(final Map<String, String> args) {
    this.buildArgs.putAll(args);
    return this;
  }

  /**
   * Sets the Dockerfile to be used for this image.
   *
   * @param relativePathFromBuildContextDirectory relative path to the Dockerfile, relative to the image build context directory
   * @deprecated It is recommended to use {@link #withDockerfile} instead because it honors .dockerignore files and
   * will therefore be more efficient. Additionally, using {@link #withDockerfile} supports Dockerfiles that depend
   * upon images from authenticated private registries.
   */
  @Deprecated
  public ReusableImageFromDockerfile withDockerfilePath(final String relativePathFromBuildContextDirectory) {
    this.dockerFilePath = relativePathFromBuildContextDirectory;
    return this;
  }

  /**
   * Sets the Dockerfile to be used for this image. Honors .dockerignore files for efficiency.
   * Additionally, supports Dockerfiles that depend upon images from authenticated private registries.
   *
   * @param dockerfile path to Dockerfile on the test host.
   */
  public ReusableImageFromDockerfile withDockerfile(final Path dockerfile) {
    this.dockerfile = dockerfile;
    return this;
  }

}
