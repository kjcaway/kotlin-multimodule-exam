package me.multimoduleexam.moduleapi.exception

import me.multimoduleexam.moduleapi.api.dto.ApiResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler


@ControllerAdvice
class CustomExceptionHandler {
    private val logger: Logger = LoggerFactory.getLogger(CustomExceptionHandler::class.java)

    @ExceptionHandler(CustomRuntimeException::class)
    fun handleCustomException(ex: CustomRuntimeException): ResponseEntity<*> {
        logger.error(ex.message, ex)

        return ResponseEntity<Any?>(ApiResult.error(ex.message), ex.getErrorCode())
    }
}