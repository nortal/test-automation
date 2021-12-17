package org.testcontainers.containers

import java.util.concurrent.Future

/**
 * Variant of [GenericContainer] that allows a fixed port on the docker host to be mapped to a container port.
 *
 *
 * **Normally this should not be required, and Docker should be allowed to choose a free host port instead**.
 * However, when a fixed host port is absolutely required for some reason, this class can be used to set it.
 *
 *
 * Callers are responsible for ensuring that this fixed port is actually available; failure will occur if it is
 * not available - which could manifest as flaky or unstable tests.
 */
class CustomFixedHostPortGenericContainer(image: Future<String?>?) : GenericContainer<CustomFixedHostPortGenericContainer>(
    image!!
) {
    /**
     * Bind a fixed port on the docker host to a container port
     * @param hostPort          a port on the docker host, which must be available
     * @param containerPort     a port in the container
     * @param protocol          an internet protocol (tcp or udp)
     * @return this container
     */
    @JvmOverloads
    fun withFixedExposedPort(
        hostPort: Int,
        containerPort: Int,
        protocol: InternetProtocol? = InternetProtocol.TCP
    ): CustomFixedHostPortGenericContainer {
        super.addFixedExposedPort(hostPort, containerPort, protocol)
        return self()
    }
}