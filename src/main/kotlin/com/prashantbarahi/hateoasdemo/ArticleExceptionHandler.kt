package com.prashantbarahi.hateoasdemo

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import kotlin.RuntimeException


class ErrorResponse(val message: String)

@ControllerAdvice
class ArticleExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(exception: DomainException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(message = exception.message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(exception: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(message = exception.message ?: "Bad request"), HttpStatus.BAD_REQUEST)
    }

    @Order(Ordered.LOWEST_PRECEDENCE)
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(exception: RuntimeException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(message = exception.message ?: "Unknown error"),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}