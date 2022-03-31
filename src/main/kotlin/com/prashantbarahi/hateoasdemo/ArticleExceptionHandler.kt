package com.prashantbarahi.hateoasdemo

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


class ErrorResponse(val message: String)

@ControllerAdvice
class ArticleExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(exception: DomainException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(ErrorResponse(message = exception.message), HttpStatus.BAD_REQUEST)
    }
}