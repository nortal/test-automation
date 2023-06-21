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
        val readTimeout: Int = 30,
        /**
         * Sets the default connect timeout for new connections.
         * The connect timeout is applied when connecting a TCP socket to the target host.
         * The default value is 30 seconds.
         */
        val connectTimeout: Int = 30,
        /**
         * Sets the default write timeout for new connections.
         * The write timeout is applied for individual write IO operations.
         * The default value is 30 seconds.
         */
        val writeTimeout: Int = 30,
        /**
         * Sets the default timeout for complete calls.
         * The call timeout spans the entire call: resolving DNS, connecting, writing the request body, server processing, and reading the response body.
         * If the call requires redirects or retries all must complete within one timeout period.
         * The default value is 30 seconds.
         */
        val callTimeout: Int = 30,
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