package io.taesu.apiclientspring.interfaces

import io.taesu.apiclientspring.infra.exception.ApiException
import io.taesu.apiclientspring.infra.exception.UnexpectedResponseException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Created by itaesu on 2024/05/24.
 *
 * @author Lee Tae Su
 * @version apiclient-spring
 * @since apiclient-spring
 */
@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(ApiException::class)
    fun handleApiException(exception: ApiException): ResponseEntity<Any> {
        return ResponseEntity.status(exception.statusCode)
            .body(exception.failResponse)
    }

    @ExceptionHandler(UnexpectedResponseException::class)
    fun handleApiException(exception: UnexpectedResponseException): ResponseEntity<Any> {
        return ResponseEntity.status(exception.statusCode)
            .body(exception.body)
    }
}
