package com.nortal.test.aws.configuration

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(AwsProperties::class)
@Configuration
open class AwsConfiguration {

    @Bean
    open fun amazonS3(
        awsProperties: AwsProperties,
        awsCredentialsProvider: AWSCredentialsProvider
    ): AmazonS3 {
        return AmazonS3ClientBuilder.standard()
            .withRegion(awsProperties.region)
            .withCredentials(awsCredentialsProvider)
            .build()
    }

    @Bean
    open fun transferManager(amazonS3: AmazonS3): TransferManager {
        return TransferManagerBuilder.standard()
            .withS3Client(amazonS3)
            .build()
    }
}