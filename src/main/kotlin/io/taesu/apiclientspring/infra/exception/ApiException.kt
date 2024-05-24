package io.taesu.apiclientspring.infra.exception

import io.taesu.apiclientspring.interfaces.dtos.FailResponse
import org.springframework.http.HttpStatusCode

/**
 * Created by itaesu on 2024/05/24.
 *
 * @author Lee Tae Su
 * @version apiclient-spring
 * @since apiclient-spring
 */
open class ApiException(
    cause: Throwable? = null,
    val statusCode: HttpStatusCode,
    val failResponse: FailResponse,
): RuntimeException(cause)
