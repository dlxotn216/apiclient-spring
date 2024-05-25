package io.taesu.apiclientspring.infra.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import io.taesu.apiclientspring.infra.exception.ApiException
import io.taesu.apiclientspring.infra.exception.UnexpectedResponseException
import io.taesu.apiclientspring.interfaces.dtos.FailResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.*
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import reactor.util.retry.Retry
import reactor.util.retry.RetryBackoffSpec
import java.time.Duration
import java.util.concurrent.TimeUnit


/**
 * Created by itaesu on 2024/05/24.
 *
 * @author Lee Tae Su
 * @version apiclient-spring
 * @since apiclient-spring
 */
@Configuration
class WebClientConfig {
    @Bean
    fun webClient(): WebClient {
        val client = getHttpClient()
        val strategies = getExchangeStrategies()
        val webClient = WebClient.builder()
            .exchangeStrategies(strategies)
            .clientConnector(ReactorClientHttpConnector(client))
            .filter { request, next ->
                next.exchange(request)
                    .flatMap { clientResponse ->
                        // 에러 시그널이 방출 되어야 retryWhen이 동작한다
                        Mono.just(clientResponse)
                            .filter { it.statusCode().isError }
                            .flatMap { it.createException() }   // Mono<Exception>은 에러 시그널이 아니다!!!
                            .flatMap {// map도 컴파일은 되나 flatMap으로 Mono를 반환해야 한다.
                                Mono.error<Throwable> {         // Mono.error를 반환해야 에러 시그널이 방출된다
                                    if (isRetryable(it)) {
                                        // 재시도 가능하다면 번역하지 않음
                                        it
                                    } else {
                                        // 재시도 불가라면 번역 처리
                                        translate(it, clientResponse)
                                    }
                                }
                            }
                            .thenReturn(clientResponse)
                    }
                    .retryWhen(defaultRetryBackoffSpec())

            }
            .defaultStatusHandler(HttpStatusCode::isError) { handleException(it) }
            .build()
        return webClient
    }


    private fun getHttpClient(name: String = "default-webclient-connection"): HttpClient {
        val provider = ConnectionProvider.builder(name)
            .maxIdleTime(Duration.ofSeconds(58))            // 미사용 시 유지 시간, 대상 서버의 idle 보다 작아야 한다
            .maxLifeTime(Duration.ofSeconds(58))            // 풀에서 유지 되는 시간
            .pendingAcquireTimeout(Duration.ofSeconds(10))  // 풀에서 커넥션 기다리는 최대 시간
            .evictInBackground(Duration.ofSeconds(120))     // 2분 주기로 유효하지 않은 커넥션 제거
            .lifo()
            .build()
        return HttpClient.create(provider)
            .wiretap(true)
            .compress(true)
            .responseTimeout(Duration.ofMillis(10000))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Duration.ofMillis(100).toMillis().toInt())
            .doOnConnected {
                it.addHandlerFirst(ReadTimeoutHandler(10000, TimeUnit.MILLISECONDS))
                it.addHandlerFirst(WriteTimeoutHandler(100, TimeUnit.MILLISECONDS))

            }
    }

    private fun getExchangeStrategies(): ExchangeStrategies {
        return ExchangeStrategies.builder()
            .apply {
                // do stub
                codecs {
                    it.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)
                }
            }.build()
    }

    fun defaultRetryBackoffSpec(): RetryBackoffSpec {
        return Retry.backoff(3, Duration.ofMillis(500))
            .filter {
                isRetryable(it)
            }
            .onRetryExhaustedThrow { spec, retrySignal ->
                val exception = retrySignal.failure()
                if (exception is WebClientResponseException) {
                    UnexpectedResponseException(
                        exception,
                        exception.statusCode,
                        exception.responseBodyAsString,
                        retrySignal.totalRetries()
                    )
                } else {
                    UnexpectedResponseException(
                        exception,
                        HttpStatus.SERVICE_UNAVAILABLE,
                        exception.message ?: exception.toString(),
                        retrySignal.totalRetries()
                    )
                }
            }
    }

    private fun isRetryable(it: Throwable?): Boolean {
        return (
            it is WebClientResponseException && it.statusCode.is5xxServerError)
            || (it is WebClientRequestException)
        // || (it.cause is ConnectTimeoutException)     // it is WebClientRequestException
        // || (it.cause is ReadTimeoutException)        // it is WebClientRequestException
        // || (it.cause is WriteTimeoutException)       // it is WebClientRequestException
    }

    private fun handleException(clientResponse: ClientResponse): Mono<Exception> {
        return clientResponse.createException()
            .map { exception ->
                translate(exception, clientResponse)
            }
    }

    private fun translate(
        exception: WebClientResponseException,
        clientResponse: ClientResponse,
    ): Exception {
        return kotlin.runCatching {
            ApiException(
                exception,
                clientResponse.statusCode(),
                exception.getResponseBodyAs(FailResponse::class.java)!!
            )
        }.getOrElse {
            UnexpectedResponseException(
                exception,
                clientResponse.statusCode(),
                exception.responseBodyAsString
            )
        }
    }
}
