package io.taesu.apiclientspring.application.dtos

/**
 * Created by itaesu on 2024/05/24.
 *
 * @author Lee Tae Su
 * @version apiclient-spring
 * @since apiclient-spring
 */
class User(
    val userKey: Long,
    val email: String,
    val name: Name,
    val avatar: String
) {
    class Name(
        val firstName: String,
        val lastName: String
    )
}
