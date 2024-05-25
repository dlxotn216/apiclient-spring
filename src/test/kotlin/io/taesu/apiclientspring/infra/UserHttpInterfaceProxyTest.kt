package io.taesu.apiclientspring.infra

import io.taesu.apiclientspring.infra.dtos.UserCreateRequest
import io.taesu.apiclientspring.infra.dtos.UserPaginatedCriteria
import io.taesu.apiclientspring.infra.exception.ApiException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/**
 * Created by itaesu on 2024/05/24.
 *
 * @author Lee Tae Su
 * @version apiclient-spring
 * @since apiclient-spring
 */
@SpringBootTest
class UserHttpInterfaceProxyTest {
    @Autowired
    private lateinit var userClientProxy: UserClientProxy

    @Test
    fun `create test`() {
        // given, when
        val result = userClientProxy.create(
            UserCreateRequest(
                "eve.holt@reqres.in",
                "pwd",
            )
        )

        // then
        result.onSuccess {
            println(it.id)
            println(it.token)
        }
    }

    @Test
    fun `create fail test`() {
        // given, when
        val result = userClientProxy.create(
            UserCreateRequest(
                "taesulee93@gmail.com",
                null
            )
        )

        when (val t = result.exceptionOrNull()) {
            is ApiException -> {
                println(t.statusCode)
            }

            else -> {
                throw IllegalStateException("Test Failure")
            }
        }

    }

    @Test
    fun `paginate test`() {
        // given, when
        val result = userClientProxy.paginate(UserPaginatedCriteria(1))

        // then
        Assertions.assertNotNull(result)
        println(result)
    }

    @Test
    fun `paginate fail test`() {
        // given, when
        val result = userClientProxy.paginate(UserPaginatedCriteria(-11123))

        // then
        Assertions.assertNotNull(result)
        println(result)
    }

    @Test
    fun `retrieveSingle test`() {
        // given, when
        val result = userClientProxy.retrieve(1L)

        // then
        Assertions.assertNotNull(result)
        println(result)
    }

    @Test
    fun `retrieveSingle Notfound test`() {
        // given, when
        when (val t = userClientProxy.retrieve(23L).exceptionOrNull()) {
            is ApiException -> {
                println(t.statusCode)
            }

            else -> {
                throw IllegalStateException("Test Failure")
            }
        }

        // then
    }
}
