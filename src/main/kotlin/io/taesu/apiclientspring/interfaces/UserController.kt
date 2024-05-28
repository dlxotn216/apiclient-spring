package io.taesu.apiclientspring.interfaces

import io.taesu.apiclientspring.application.UserService
import io.taesu.apiclientspring.infra.dtos.UserCreateRequest
import io.taesu.apiclientspring.infra.dtos.UserPaginatedCriteria
import io.taesu.apiclientspring.infra.dtos.UserPaginationResponse
import io.taesu.apiclientspring.infra.dtos.UserSingleResponse
import org.springframework.web.bind.annotation.*

/**
 * Created by itaesu on 2024/05/24.
 *
 * @author Lee Tae Su
 * @version apiclient-spring
 * @since apiclient-spring
 */
@RestController
class UserController(private val userService: UserService) {
    @PostMapping("/api/v1/users")
    fun create(@RequestBody request: UserCreateRequest): Long {
        return userService.create(request)
    }

    @PutMapping("/api/v1/users")
    fun merge(@RequestBody request: UserCreateRequest) {
        return userService.merge(request)
    }

    @GetMapping("/api/v1/users")
    fun paginate(criteria: UserPaginatedCriteria): UserPaginationResponse {
        return userService.paginate(criteria)
    }

    @GetMapping("/api/v1/users/{userKey}")
    fun retrieve(@PathVariable userKey: Long): UserSingleResponse {
        return userService.retrieve(userKey)
    }
}
