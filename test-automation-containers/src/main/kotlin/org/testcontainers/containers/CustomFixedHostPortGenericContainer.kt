/**
 * Copyright (c) 2022 Nortal AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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