package com.nortal.test.core.exceptions

import java.lang.RuntimeException

/**
 * A generic exception to be thrown when continuing scenario execution is no longer possible
 */
open class TestExecutionException : RuntimeException {
    constructor(message: String?) : super(message) {}
    constructor(cause: Throwable?) : super(cause) {}
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
}