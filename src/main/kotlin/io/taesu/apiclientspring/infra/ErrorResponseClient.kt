package io.taesu.apiclientspring.infra

import org.springframework.http.ResponseEntity

/**
 * Created by itaesu on 2024. 6. 28..
 *
 * @author Lee Tae Su
 * @version apiclient-spring
 * @since apiclient-spring
 */
interface ErrorResponseClient {
    fun error404(): ResponseEntity<Any>
    fun error502(): ResponseEntity<Any>
}
