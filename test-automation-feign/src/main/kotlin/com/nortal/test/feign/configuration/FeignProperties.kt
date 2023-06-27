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
package com.nortal.test.feign.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "test-automation.feign")
data class FeignProperties(
        /**
         * Sets the default read timeout for new connections.
         * The read timeout is applied to both the TCP socket and for individual read IO operations.
         * The default value is 30 seconds.
         */
        val readTimeout: Long = 30,
        /**
         * Sets the default connect timeout for new connections.
         * The connect timeout is applied when connecting a TCP socket to the target host.
         * The default value is 30 seconds.
         */
        val connectTimeout: Long = 30,
        /**
         * Sets the default write timeout for new connections.
         * The write timeout is applied for individual write IO operations.
         * The default value is 30 seconds.
         */
        val writeTimeout: Long = 30,
        /**
         * Sets the default timeout for complete calls.
         * The call timeout spans the entire call: resolving DNS, connecting, writing the request body,
         * server processing, and reading the response body.
         * If the call requires redirects or retries all must complete within one timeout period.
         * The default value is 30 seconds.
         */
        val callTimeout: Long = 30,
        /**
         * Configure this client to retry or not when a connectivity problem is encountered.
         * The default value is true.
         */
        val retryOnConnectionFailure: Boolean = true,
        /**
         * Configure this client to follow redirects.
         * The default value is true.
         */
        val followRedirects: Boolean = true,
        /**
         * Configure this client to allow protocol redirects from HTTPS to HTTP and from HTTP to HTTPS.
         * Redirects are still first restricted by followRedirects.
         * The default value is true.
         */
        val followSslRedirects: Boolean = true
)
