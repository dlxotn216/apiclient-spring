package io.taesu.apiclientspring.infra

import org.springframework.http.ResponseEntity
import org.springframework.web.service.annotation.GetExchange

/**
 * Created by itaesu on 2024. 6. 28..
 *
 * @author Lee Tae Su
 * @version apiclient-spring
 * @since apiclient-spring
 */
interface ErrorHttpInterface {
    @GetExchange("/api/v1/errors/404")
    fun error404(): ResponseEntity<Any>

    @GetExchange("/api/v1/errors/502")
    fun error502(): ResponseEntity<Any>
}
