package com.nortal.test.core.model

import org.springframework.http.HttpStatus

class CommonCallResponse<S, E> private constructor(val successResponse: S, errorResponse: E, httpStatus: HttpStatus) {
    val errorResponse: E?
    val httpStatus: HttpStatus

    init {
        this.errorResponse = errorResponse
        this.httpStatus = httpStatus
    }

    val isError: Boolean
        get() = errorResponse != null

    companion object {
        fun <S, E> onSuccess(successResponse: S, httpStatus: HttpStatus): CommonCallResponse<S, E?> {
            return CommonCallResponse(successResponse, null, httpStatus)
        }

        fun <S, E> onError(errorResponse: E, httpStatus: HttpStatus): CommonCallResponse<S?, E> {
            return CommonCallResponse(null, errorResponse, httpStatus)
        }
    }
}