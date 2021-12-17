package com.nortal.test.core.exceptions

class GoldenDataLockException : TestExecutionException {
    constructor(message: String?) : super(message) {}
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
}