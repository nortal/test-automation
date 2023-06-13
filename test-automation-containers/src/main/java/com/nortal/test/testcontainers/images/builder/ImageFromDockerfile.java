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
package com.nortal.test.testcontainers.images.builder;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.images.ParsedDockerfile;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.images.builder.traits.*;
import org.testcontainers.utility.Base58;
import org.testcontainers.utility.DockerLoggerFactory;
import org.testcontainers.utility.LazyFuture;
import org.testcontainers.utility.ResourceReaper;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 * This is a direct copy of 1.16.2 version testcontainers. We're changing method visibility so for {@link  ReusableImageFromDockerfile}
 * implementation.
 */
public class ImageFromDockerfile extends LazyFuture<String>
        implements BuildContextBuilderTrait<ImageFromDockerfile>, ClasspathTrait<ImageFromDockerfile>, FilesTrait<ImageFromDockerfile>,
        StringsTrait<ImageFromDockerfile>, DockerfileTrait<ImageFromDockerfile> {
    private final String dockerImageName;

    private boolean deleteOnExit = true;

    private final Map<String, Transferable> transferables = new HashMap<>();
    private final Map<String, String> buildArgs = new HashMap<>();
    private Optional<String> dockerFilePath = Optional.empty();
    private Optional<Path> dockerfile = Optional.empty();
    private Set<String> dependencyImageNames = Collections.emptySet();

    public ImageFromDockerfile() {
        this("localhost/testcontainers/" + Base58.randomString(16).toLowerCase());
    }

    public ImageFromDockerfile(String dockerImageName) {
        this(dockerImageName, true);
    }

    public ImageFromDockerfile(String dockerImageName, boolean deleteOnExit) {
        this.dockerImageName = dockerImageName;
        this.deleteOnExit = deleteOnExit;
    }

    @Override
    public ImageFromDockerfile withFileFromTransferable(String path, Transferable transferable) {
        Transferable oldValue = transferables.put(path, transferable);

        if (oldValue != null) {
			logger().warn("overriding previous mapping for '{}'", path);
        }

        return this;
    }

    @Override
    protected final String resolve() {
		Logger logger = logger();
        DockerClient dockerClient = DockerClientFactory.instance().client();

        try (PipedInputStream in = new PipedInputStream();
             PipedOutputStream out = new PipedOutputStream(in)) {

            if (deleteOnExit) {
                ResourceReaper.instance().registerImageForCleanup(dockerImageName);
            }

            BuildImageResultCallback resultCallback = new BuildImageResultCallback() {
                @Override
                public void onNext(BuildResponseItem item) {
                    super.onNext(item);

                    if (item.isErrorIndicated()) {
                        logger.error(item.getErrorDetail().getMessage());
                    } else {
                        logger.info(StringUtils.chomp(item.getStream(), "\n"));
                    }
                }
            };

            // We have to use pipes to avoid high memory consumption since users might want to build really big images


            BuildImageCmd buildImageCmd = dockerClient.buildImageCmd(in);
            configure(buildImageCmd);

            //Extension point.
            addApplicableLabels(buildImageCmd);

            prePullDependencyImages(dependencyImageNames);

            BuildImageResultCallback exec = buildImageCmd.exec(resultCallback);

            long bytesToDockerDaemon = 0;

            // To build an image, we have to send the context to Docker in TAR archive format
            try (TarArchiveOutputStream tarArchive = new TarArchiveOutputStream(new GZIPOutputStream(out))) {
                tarArchive.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
                tarArchive.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);

                for (Map.Entry<String, Transferable> entry : transferables.entrySet()) {
                    Transferable transferable = entry.getValue();
                    final String destination = entry.getKey();
                    transferable.transferTo(tarArchive, destination);
                    bytesToDockerDaemon += transferable.getSize();
                }
                tarArchive.finish();
            }

            logger.info("Transferred {} to Docker daemon", FileUtils.byteCountToDisplaySize(bytesToDockerDaemon));
            if (bytesToDockerDaemon > FileUtils.ONE_MB * 50) // warn if >50MB sent to docker daemon
                logger.warn("A large amount of data was sent to the Docker daemon ({}). Consider using a .dockerignore file for better performance.",
                        FileUtils.byteCountToDisplaySize(bytesToDockerDaemon));

            exec.awaitImageId();

            return dockerImageName;
        } catch (IOException e) {
            throw new RuntimeException("Can't close DockerClient", e);
        }
    }

    protected void addApplicableLabels(BuildImageCmd buildImageCmd) {
        Map<String, String> labels = new HashMap<>();
        if (buildImageCmd.getLabels() != null) {
            labels.putAll(buildImageCmd.getLabels());
        }
        labels.putAll(DockerClientFactory.DEFAULT_LABELS);
        if (deleteOnExit) {
            //noinspection deprecation
            labels.putAll(ResourceReaper.instance().getLabels());
        }

        buildImageCmd.withLabels(labels);
    }

    protected void configure(BuildImageCmd buildImageCmd) {
        buildImageCmd.withTag(this.getDockerImageName());
        this.dockerFilePath.ifPresent(buildImageCmd::withDockerfilePath);
        this.dockerfile.ifPresent(p -> {
            buildImageCmd.withDockerfile(p.toFile());
            dependencyImageNames = new ParsedDockerfile(p).getDependencyImageNames();

            if (dependencyImageNames.size() > 0) {
                // if we'll be pre-pulling images, disable the built-in pull as it is not necessary and will fail for
                // authenticated registries
                buildImageCmd.withPull(false);
            }
        });

        this.buildArgs.forEach(buildImageCmd::withBuildArg);
    }

    private void prePullDependencyImages(Set<String> imagesToPull) {
        final DockerClient dockerClient = DockerClientFactory.instance().client();

        imagesToPull.forEach(imageName -> {
            try {
				logger().info("Pre-emptively checking local images for '{}', referenced via a Dockerfile. If not available, it will be pulled.",
                        imageName);
                DockerClientFactory.instance().checkAndPullImage(dockerClient, imageName);
            } catch (Exception e) {
                logger().warn("Unable to pre-fetch an image ({}) depended upon by Dockerfile - image build will continue but may fail. Exception message "
                        + "was: {}", imageName, e.getMessage());
            }
        });
    }

    public ImageFromDockerfile withBuildArg(final String key, final String value) {
        this.buildArgs.put(key, value);
        return this;
    }

    public ImageFromDockerfile withBuildArgs(final Map<String, String> args) {
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
    public ImageFromDockerfile withDockerfilePath(String relativePathFromBuildContextDirectory) {
        this.dockerFilePath = Optional.of(relativePathFromBuildContextDirectory);
        return this;
    }

    /**
     * Sets the Dockerfile to be used for this image. Honors .dockerignore files for efficiency.
     * Additionally, supports Dockerfiles that depend upon images from authenticated private registries.
     *
     * @param dockerfile path to Dockerfile on the test host.
     */
    public ImageFromDockerfile withDockerfile(Path dockerfile) {
        this.dockerfile = Optional.of(dockerfile);
        return this;
    }

    public String getDockerImageName() {
        return dockerImageName;
    }

    public String getDockerImageNameWithVersion() {
        return dockerImageName + ":latest";
    }

    private Logger logger() {
        return DockerLoggerFactory.getLogger(dockerImageName);
    }
}