package no.nav.gjenlevende.bs.sak.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient

const val PDL_CLIENT_REGISTRATION_ID = "pdl-clientcredentials"

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
    open fun pdlClientRegistration(
        @Value("\${azure.app.client.id}")
        clientId: String,
        @Value("\${azure.app.client.secret}")
        clientSecret: String,
        @Value("\${AZUREAD_TOKEN_ENDPOINT_URL}")
        tokenEndpointUrl: String,
        @Value("\${PDL_SCOPE}")
        scope: String,
    ): ClientRegistration {
        val scopes =
            scope
                .split(",")
                .map(String::trim)
                .filter(String::isNotEmpty)

        return ClientRegistration
            .withRegistrationId(PDL_CLIENT_REGISTRATION_ID)
            .tokenUri(tokenEndpointUrl)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope(*scopes.toTypedArray())
            .build()
    }

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

    @Bean(name = ["azureClientCredential"])
    open fun azureClientCredentialRestTemplate(
        authorizedClientManager: OAuth2AuthorizedClientManager,
    ): RestOperations {
        val restTemplate = RestTemplate()
        restTemplate.interceptors.add(PdlBearerTokenInterceptor(authorizedClientManager))
        return restTemplate
    }
}

internal class PdlBearerTokenInterceptor(
    private val authorizedClientManager: OAuth2AuthorizedClientManager,
) : ClientHttpRequestInterceptor {
    private val logger = LoggerFactory.getLogger(PdlBearerTokenInterceptor::class.java)

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        val token =
            authorizedClientManager
                .authorize(
                    OAuth2AuthorizeRequest
                        .withClientRegistrationId(PDL_CLIENT_REGISTRATION_ID)
                        .principal("gjenlevende-bs-sak")
                        .build(),
                )?.accessToken
                ?.tokenValue

        return if (token.isNullOrBlank()) {
            logger.error("Kunne ikke hente token for PDL")
            throw IllegalStateException("Kunne ikke hente token for PDL")
        } else {
            request.headers.setBearerAuth(token)
            execution.execute(request, body)
        }
    }
}
