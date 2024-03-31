package me.multimoduleexam.moduleapi.api.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
open class ApiResult<T> {
    val success: Boolean
    val data: T?
    val message: String?

    constructor(success: Boolean) {
        this.success = success
        this.data = null
        this.message = null
    }

    constructor(success: Boolean, data: T) {
        this.success = success
        this.data = data
        this.message = null
    }

    constructor(success: Boolean, data: T, message: String?) {
        this.success = success
        this.data = data
        this.message = message
    }

    companion object {
        fun ok(): ApiResult<*> {
            return ApiResult<Any>(true)
        }

        fun <T> ok(data: T): ApiResult<T> {
            return ApiResult(true, data)
        }

        fun error(message: String?): ApiResult<*> {
            return ApiResult<Any?>(false, null, message)
        }
    }
}