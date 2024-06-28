package io.taesu.apiclientspring.infra

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

/**
 * Created by itaesu on 2024. 6. 28..
 *
 * @author Lee Tae Su
 * @version apiclient-spring
 * @since apiclient-spring
 */
@Component
class ErrorResponseClientProxy(
    webClient: WebClient,
): ErrorResponseClient {
    private final val errorHttpInterface: ErrorHttpInterface

    init {
        val httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(
            WebClientAdapter.create(webClient.mutate().baseUrl("http://localhost:8080").build())
        ).build()
        this.errorHttpInterface = httpServiceProxyFactory.createClient(ErrorHttpInterface::class.java)
    }

    override fun error404() = errorHttpInterface.error404()

    override fun error502() = errorHttpInterface.error502()
}
