package io.taesu.apiclientspring.infra

import io.taesu.apiclientspring.infra.dtos.*

/**
 * Created by itaesu on 2024/05/24.
 *
 * @author Lee Tae Su
 * @version apiclient-spring
 * @since apiclient-spring
 */

interface UserClient {
    fun create(request: UserCreateRequest): Result<UserCreateResponse>
    fun createForTest(request: UserCreateRequest): Result<UserCreateResponse>
    fun paginate(criteria: UserPaginatedCriteria): Result<UserPaginationResponse>
    fun retrieve(usrKey: Long): Result<UserSingleResponse>
}
