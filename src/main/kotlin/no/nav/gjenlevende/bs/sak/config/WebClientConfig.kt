package no.nav.gjenlevende.bs.sak.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

@Configuration
open class WebClientConfig {
    @Bean
    open fun infotrygdWebClient(
        @Value("\${gjenlevende-bs-infotrygd.url}")
        infotrygdUrl: String,
    ): WebClient =
        WebClient
            .builder()
            .baseUrl(infotrygdUrl)
            .defaultHeader("Content-Type", "application/json")
            .filter(tokenPropageringFilter())
            .build()

    private fun tokenPropageringFilter(): ExchangeFilterFunction =
        ExchangeFilterFunction { request, next ->
            val authentication = SecurityContextHolder.getContext().authentication

            if (authentication is JwtAuthenticationToken) {
                val token = authentication.token.tokenValue

                val mutertRequest =
                    ClientRequest
                        .from(request)
                        .header("Authorization", "Bearer $token")
                        .build()

                next.exchange(mutertRequest)
            } else {
                next.exchange(request)
            }
        }
}
