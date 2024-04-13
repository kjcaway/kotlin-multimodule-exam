package me.multimoduleexam.moduleapi.exception

import me.multimoduleexam.moduleapi.api.dto.ApiResult
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


@ControllerAdvice
class CustomExceptionHandler : ResponseEntityExceptionHandler() {

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        logger.error(ex.message, ex)
        return ResponseEntity<Any>(ApiResult.error(ex.message), HttpStatus.BAD_REQUEST)
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        logger.error(ex.message, ex)
        return ResponseEntity<Any>(ApiResult.error(ex.message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(CustomRuntimeException::class)
    fun handleCustomException(ex: CustomRuntimeException): ResponseEntity<*> {
        logger.error(ex.message, ex)

        return ResponseEntity<Any>(ApiResult.error(ex.message), ex.getErrorCode())
    }
}