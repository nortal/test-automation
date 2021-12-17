package com.nortal.test.core.services

import com.nortal.test.core.services.hooks.AfterSuiteHook
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.annotation.PreDestroy

@Component
class AfterSuiteHookTrigger(private val afterSuiteHooks: List<AfterSuiteHook>) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PreDestroy
    fun afterSuite() {
        afterSuiteHooks.forEach {
            log.info("Executing @AfterAll action {}", it.javaClass.simpleName)
            it.afterSuite()
        }
    }
}