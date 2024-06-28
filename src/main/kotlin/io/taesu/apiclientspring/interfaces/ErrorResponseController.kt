package io.taesu.apiclientspring.interfaces

import io.taesu.apiclientspring.infra.ErrorResponseClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created by itaesu on 2024. 6. 28..
 *
 * @author Lee Tae Su
 * @version apiclient-spring
 * @since apiclient-spring
 */
@RestController
class ErrorResponseController {
    @GetMapping("/api/v1/errors/404")
    fun error404() = ResponseEntity.status(404).body("not found")

    @GetMapping("/api/v1/errors/502")
    fun error502() = ResponseEntity.status(502).body("502 Bad Gateway")
}


@RestController
class ErrorRequestController(private val errorResponseClient: ErrorResponseClient) {
    @GetMapping("/api/v1/call/404")
    fun error404() = errorResponseClient.error404()

    @GetMapping("/api/v1/call/502")
    fun error502() = errorResponseClient.error502()
}
