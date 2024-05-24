package io.taesu.apiclientspring.interfaces.dtos

/**
 * Created by itaesu on 2024/05/24.
 *
 * @author Lee Tae Su
 * @version apiclient-spring
 * @since apiclient-spring
 */
class FailResponse(
    val errorCode: String,
    val errorDetails: String? = null,
    val debugMessage: String? = null,
)
