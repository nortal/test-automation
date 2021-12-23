package com.nortal.test.feign

import feign.Feign
import feign.Target.HardCodedTarget
import org.springframework.cloud.openfeign.FeignClientFactoryBean
import org.springframework.cloud.openfeign.FeignContext
import org.springframework.cloud.openfeign.Targeter

class DefaultTargeter : Targeter {
    override fun <T> target(
        factory: FeignClientFactoryBean, feign: Feign.Builder, context: FeignContext,
        target: HardCodedTarget<T>
    ): T {
        return feign.target(target)
    }
}