package com.nortal.test.core.rest.error

import java.lang.RuntimeException

/**
 * Exception representing generic validation failure.
 */
open class ValidationException(message: String?) : RuntimeException(message)