package io.taesu.apiclientspring.infra.config.aop

import com.fasterxml.jackson.databind.ObjectMapper
import io.taesu.apiclientspring.infra.exception.ApiException
import io.taesu.apiclientspring.interfaces.dtos.FailResponse
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import kotlin.reflect.full.isSubclassOf


/**
 * Created by itaesu on 2024/05/24.
 *
 * @author Lee Tae Su
 * @version apiclient-spring
 * @since apiclient-spring
 */
@Aspect
@Component
class ApiProxyAspect(
    private val apiExceptionTranslator: ApiExceptionTranslator,
) {
    @Around("within(@io.taesu.apiclientspring.infra.config.aop.ApiProxy *)")
    fun beforeMethodsInAnnotatedClass(joinPoint: ProceedingJoinPoint): Any? {
        return joinPoint.proceed().apply {
            if (this is Result<*>) { // not working, Result type is value class
                this.recoverCatching {
                    apiExceptionTranslator.translate(it)
                }
            }
        }
    }
}

interface ApiExceptionTranslator {
    fun translate(throwable: Throwable): ApiException
}

@Component
class WebClientResponseExceptionTranslator(
    private val objectMapper: ObjectMapper,
): ApiExceptionTranslator {
    override fun translate(throwable: Throwable): ApiException {
        return if (throwable is WebClientResponseException) {
            ApiException(
                throwable.cause,
                throwable.statusCode,
                failResponse(throwable)
            )
        } else {
            throw throwable
        }
    }

    private fun failResponse(it: WebClientResponseException): FailResponse =
        kotlin.runCatching {
            objectMapper.readValue(it.responseBodyAsString, FailResponse::class.java)
        }.getOrElse {
            FailResponse("UNKNOWN_ERROR", "Unknown error occurred")
        }
}
