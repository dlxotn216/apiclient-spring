package io.taesu.apiclientspring.infra

import com.fasterxml.jackson.databind.ObjectMapper
import io.taesu.apiclientspring.infra.dtos.UserCreateRequest
import io.taesu.apiclientspring.infra.exception.ApiException
import io.taesu.apiclientspring.infra.exception.UnexpectedResponseException
import io.taesu.apiclientspring.interfaces.dtos.FailResponse
import okhttp3.Headers
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier


/**
 * Created by itaesu on 2024/05/24.
 *
 * @author Lee Tae Su
 * @version apiclient-spring
 * @since apiclient-spring
 */
@SpringBootTest
class UserClientProxyTest {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var webClient: WebClient

    private lateinit var userClientProxy: UserClientProxy

    private lateinit var mockWebServer: MockWebServer

    private lateinit var mockWebServerUrl: String

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        mockWebServerUrl = mockWebServer.url("").toString()
        userClientProxy = UserClientProxy(mockWebServerUrl, webClient)
    }

    protected fun jsonBody(body: Any?): String {
        return objectMapper.writeValueAsString(body)
    }

    @Test
    fun `공통 Api Response로 변환 성공한다`() {
        // given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(409)
                .setHeaders(
                    Headers.Builder().add(
                        "Content-Type",
                        MediaType.APPLICATION_JSON_VALUE
                    ).build()
                )
                .setBody(jsonBody(FailResponse("DUPLICATED")))
        )

        // when
        val result = userClientProxy.create(
            UserCreateRequest(
                "taesu",
                "password"
            )
        )

        // then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(ApiException::class.java)
        val apiException = result.exceptionOrNull() as ApiException
        assertThat(apiException.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(apiException.failResponse.errorCode).isEqualTo("DUPLICATED")
    }

    @Test
    fun `공통 Api Response로 변환 실패 시 body를 예외에 담아 전달한다`() {
        // given
        val mockBody = """
            {
                "code": "DUPLICATED",
                "message": "User already exists"
            }
            """.trimIndent()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(409)
                .setHeaders(
                    Headers.Builder().add(
                        "Content-Type",
                        MediaType.APPLICATION_JSON_VALUE
                    ).build()
                )
                .setBody(mockBody)
        )

        // when
        val result = userClientProxy.create(
            UserCreateRequest(
                "taesu",
                "password"
            )
        )

        // then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(UnexpectedResponseException::class.java)

        val apiException = result.exceptionOrNull() as UnexpectedResponseException
        assertThat(apiException.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(apiException.body).isEqualTo(mockBody)
    }

    @Test
    fun `500 Error 에 따른 에러는 재시도 처리 한다`() {
        // given
        repeat(5) {
            mockWebServer.enqueue(
                MockResponse().setResponseCode(500)
            )
        }

        // when
        val result = webClient.post()
            .uri(mockWebServerUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("")
            .retrieve()
            .bodyToMono(String::class.java)

        // then
        StepVerifier.create(result)
            .verifyError()
        assertThat(mockWebServer.requestCount).isEqualTo(4)
    }

    @Test
    fun `ConnectionTimeout에 따른 에러는 재시도 처리 한다`() {
        // given
        repeat(4) {
            mockWebServer.enqueue(
                MockResponse().setResponseCode(500)
            )
        }

        // when
        val result = webClient.post()
            .uri("http://localhost:91")    // Connection timeout
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("")
            .retrieve()
            .bodyToMono(String::class.java)


        // then
        StepVerifier.create(result)
            .verifyErrorMatches {
                it is UnexpectedResponseException && it.retryCount == 3L
            }

        // mockWebServer에게 요청이 가진 않음
        assertThat(mockWebServer.requestCount).isEqualTo(0)
    }

    @Test
    fun `Responseimeout에 따른 에러는 재시도 처리 한다`() {
        // given
        repeat(4) {
            mockWebServer.enqueue(
                MockResponse().setResponseCode(200)
                    .setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.NO_RESPONSE)
                    .setBodyDelay(15L, java.util.concurrent.TimeUnit.SECONDS)
            )
        }

        // when
        val result = webClient.post()
            .uri(mockWebServerUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("")
            .retrieve()
            .bodyToMono(String::class.java)


        // then
        StepVerifier.create(result)
            .verifyErrorMatches {
                it is UnexpectedResponseException && it.retryCount == 3L
            }

        // mockWebServer에게 요청이 전달 됨
        assertThat(mockWebServer.requestCount).isEqualTo(4)
    }

    @AfterEach
    fun terminate() {
        mockWebServer.shutdown()
    }

}
