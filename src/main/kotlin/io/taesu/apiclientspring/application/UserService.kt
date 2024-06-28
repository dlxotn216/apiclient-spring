package io.taesu.apiclientspring.application

import io.taesu.apiclientspring.infra.UserClient
import io.taesu.apiclientspring.infra.dtos.UserCreateRequest
import io.taesu.apiclientspring.infra.dtos.UserPaginatedCriteria
import io.taesu.apiclientspring.infra.dtos.UserPaginationResponse
import io.taesu.apiclientspring.infra.dtos.UserSingleResponse
import io.taesu.apiclientspring.infra.exception.ApiException
import org.springframework.stereotype.Service

/**
 * Created by itaesu on 2024/05/24.
 *
 * @author Lee Tae Su
 * @version apiclient-spring
 * @since apiclient-spring
 */
@Service
class UserService(private val userClient: UserClient) {
    fun create(request: UserCreateRequest): Long {
        return userClient.create(request)
            .map { it.id }
            .onApiException {
                // ApiException에 따라 처리해야하는 경우
                val failResponse = it.failResponse ?: throw IllegalStateException("Unexpected exception", it)
                if (failResponse.errorCode == "DUPLICATED_USER") {
                    throw IllegalArgumentException("User already exists")
                }
            }
    }

    fun merge(request: UserCreateRequest) {
        userClient.createForTest(request)
            .map { it.id }
            .onFailure {
                if(it is ApiException && it.failResponse.errorCode == "DUPLICATED_USER") {
                    // ignore
                } else {
                    throw it
                }
            }
    }

    fun paginate(criteria: UserPaginatedCriteria): UserPaginationResponse {
        return userClient.paginate(criteria)
            .getOrElse { UserPaginationResponse.empty() }
    }

    fun retrieve(usrKey: Long): UserSingleResponse {
        return userClient.retrieve(usrKey).getOrThrow()
    }
}

fun <T> Result<T>.onApiException(block: (exception: ApiException) -> Unit): T {
    return this.onFailure {
        when (it is ApiException) {
            true -> block(it)
            false -> throw IllegalStateException("Unexpected exception", it)
        }
    }.getOrThrow()
}
