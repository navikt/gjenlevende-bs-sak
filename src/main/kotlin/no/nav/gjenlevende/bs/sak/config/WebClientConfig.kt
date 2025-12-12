package no.nav.gjenlevende.bs.sak.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
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
            .build()

    @Bean
    open fun authorizedClientService(
        clientRegistrationRepository: ClientRegistrationRepository,
    ): OAuth2AuthorizedClientService = InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)

    @Bean
    open fun authorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService,
    ): OAuth2AuthorizedClientManager {
        val provider =
            OAuth2AuthorizedClientProviderBuilder
                .builder()
                .clientCredentials()
                .build()

        return AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService)
            .also { it.setAuthorizedClientProvider(provider) }
    }
}
