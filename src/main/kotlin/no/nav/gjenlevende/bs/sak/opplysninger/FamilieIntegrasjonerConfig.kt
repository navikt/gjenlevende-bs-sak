package no.nav.gjenlevende.bs.sak.opplysninger

import no.nav.gjenlevende.bs.sak.config.PDL_CLIENT_REGISTRATION_ID
import org.hibernate.validator.constraints.URL
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType

@Configuration
class FamilieIntegrasjonerConfig(
    @Value("\${FAMILIE_INTEGRASJONER_URL}") private val url: String,
    @Value("\${FAMILIE_INTEGRASJONER_SCOPE}") private val scope: String,
) {
    @Bean
    open fun familieIntegrasjonerClientRegistration(
        @Value("\${azure.app.client.id}")
        clientId: String,
        @Value("\${azure.app.client.secret}")
        clientSecret: String,
        @Value("\${AZUREAD_TOKEN_ENDPOINT_URL}")
        tokenEndpointUrl: String,
        @Value("\${FAMILIE_INTEGRASJONER_SCOPE}")
        scope: String,
    ): ClientRegistration {
        val scopes =
            scope
                .split(",")
                .map(String::trim)
                .filter(String::isNotEmpty)

        return ClientRegistration
            .withRegistrationId(FAMILIE_INTEGRASJONER_CLIENT_REGISTRATION_ID)
            .tokenUri(tokenEndpointUrl)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope(scope)
            .build()
    }

    companion object {
        const val FAMILIE_INTEGRASJONER_CLIENT_REGISTRATION_ID = "familie-integrasjoner-clientcredentials"
    }
}
