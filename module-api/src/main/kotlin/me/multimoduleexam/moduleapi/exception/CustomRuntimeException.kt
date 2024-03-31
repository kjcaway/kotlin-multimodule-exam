package me.multimoduleexam.moduleapi.exception

import org.springframework.http.HttpStatusCode

class CustomRuntimeException : RuntimeException {
    private lateinit var errorCode: HttpStatusCode

    constructor() : super()
    constructor(customErrorCode: HttpStatusCode) : super(customErrorCode.toString()) {
        this.errorCode = customErrorCode
    }

    constructor(customErrorCode: HttpStatusCode, message: String) : super(message) {
        this.errorCode = customErrorCode
    }

    constructor(customErrorCode: HttpStatusCode, message: String, throwable: Throwable) : super(message, throwable) {
        this.errorCode = customErrorCode
    }

    fun getErrorCode(): HttpStatusCode {
        return this.errorCode
    }
}