package com.nortal.test.core.exception

import java.lang.Exception


class TestAutomationException(message: String?, exception: Exception? = null) : RuntimeException(message, exception)