package com.nortal.test.aws.configuration

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.util.StringUtils
import org.springframework.stereotype.Component

/**
 * [AWSCredentialsProvider] implementation that provides credentials by
 * looking at the `aws.accessKeyId` and `aws.secretKey`
 * Java system properties.
 */
@Component
class AwsCredentialsProvider(
    private val awsProperties: AwsProperties
) : AWSCredentialsProvider {
    override fun getCredentials(): AWSCredentials {
        val accessKey = StringUtils.trim(awsProperties.accessKey)
        val secretKey = StringUtils.trim(awsProperties.secretKey)

        return BasicAWSCredentials(accessKey, secretKey)
    }

    override fun refresh() {
        //do nothing
    }

    override fun toString(): String {
        return javaClass.simpleName
    }
}