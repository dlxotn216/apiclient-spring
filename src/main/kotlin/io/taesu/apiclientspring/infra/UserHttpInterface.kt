package io.taesu.apiclientspring.infra

import io.taesu.apiclientspring.infra.dtos.UserCreateRequest
import io.taesu.apiclientspring.infra.dtos.UserCreateResponse
import io.taesu.apiclientspring.infra.dtos.UserPaginationResponse
import io.taesu.apiclientspring.infra.dtos.UserSingleResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.PostExchange


/**
 * Created by itaesu on 2024/05/24.
 *
 * @author Lee Tae Su
 * @version apiclient-spring
 * @since apiclient-spring
 */
interface UserHttpInterface {
    @PostExchange(
        "/api/register",
        accept = [MediaType.APPLICATION_JSON_VALUE],
        contentType = MediaType.APPLICATION_JSON_VALUE
    )
    fun create(@RequestBody request: UserCreateRequest): UserCreateResponse

    @GetExchange("/api/users", accept = [MediaType.APPLICATION_JSON_VALUE])
    fun paginate(@RequestParam("page") page: Int): UserPaginationResponse

    @GetExchange("/api/users/{userKey}", accept = [MediaType.APPLICATION_JSON_VALUE])
    fun retrieve(@PathVariable("userKey") page: Long): UserSingleResponse
}
