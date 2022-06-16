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