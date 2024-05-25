package io.taesu.apiclientspring.infra.exception

import org.springframework.http.HttpStatusCode

/**
 * Created by itaesu on 2024/05/24.
 *
 * @author Lee Tae Su
 * @version apiclient-spring
 * @since apiclient-spring
 */
class UnexpectedResponseException(
    cause: Throwable? = null,
    val statusCode: HttpStatusCode,
    val body: String,
    val retryCount: Long = 0L
): RuntimeException(cause)
