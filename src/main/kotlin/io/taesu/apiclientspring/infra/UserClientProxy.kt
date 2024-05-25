package io.taesu.apiclientspring.infra

import io.taesu.apiclientspring.infra.dtos.*
import io.taesu.apiclientspring.infra.exception.ApiException
import io.taesu.apiclientspring.interfaces.dtos.FailResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Component
class UserClientProxy(
    @Value("\${userClientBaseUrl:https://reqres.in}") baseUrl: String,
    webClient: WebClient,
): UserClient {
    private final val userHttpInterface: UserHttpInterface

    init {
        val httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(
            WebClientAdapter.create(webClient.mutate().baseUrl(baseUrl).build())
        ).build()
        this.userHttpInterface = httpServiceProxyFactory.createClient(UserHttpInterface::class.java)
    }


    override fun create(request: UserCreateRequest): Result<UserCreateResponse> {
        return runCatching {
            userHttpInterface.create(request)
        }
    }

    override fun createForTest(request: UserCreateRequest): Result<UserCreateResponse> {
        return Result.failure(
            ApiException(
                null,
                HttpStatusCode.valueOf(409),
                FailResponse("DUPLICATED_USER", "User already exists")
            )
        )
    }

    override fun paginate(criteria: UserPaginatedCriteria): Result<UserPaginationResponse> {
        return with(criteria) {
            kotlin.runCatching {
                userHttpInterface.paginate(page)
            }
        }
    }

    override fun retrieve(usrKey: Long): Result<UserSingleResponse> {
        return kotlin.runCatching {
            userHttpInterface.retrieve(usrKey)
        }
    }
}
