package com.nortal.test.aws.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * AWS module configuration.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "test-automation.aws")
data class AwsProperties(
    /**
     * S3 bucket name that will be used for actions like publishing report.
     */
    val s3BucketName: String = "",
    /**
     * AWS region.
     */
    val region: String,
    /**
     * AWS access key.
     */
    val accessKey: String,
    /**
     * AWS secret key.
     */
    val secretKey: String,
)