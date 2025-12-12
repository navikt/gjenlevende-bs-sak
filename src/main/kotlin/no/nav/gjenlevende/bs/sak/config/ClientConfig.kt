package no.nav.gjenlevende.bs.sak.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType

@Configuration
@EnableConfigurationProperties(OAuth2ClientsProperties::class)
class ClientConfig {
    @Bean
    open fun clientRegistrationRepository(props: OAuth2ClientsProperties): ClientRegistrationRepository {
        val registrations =
            props.clients.values.map { clientConfig ->
                ClientRegistration
                    .withRegistrationId(clientConfig.registrationId ?: throw IllegalStateException("missing registrationId"))
                    .tokenUri(clientConfig.tokenEndpointUrl)
                    .clientId(clientConfig.clientId)
                    .clientSecret(clientConfig.clientSecret)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .scope(clientConfig.scope)
                    .build()
            }
        return InMemoryClientRegistrationRepository(registrations)
    }
}

@ConfigurationProperties(prefix = "oauth2")
data class OAuth2ClientsProperties(
    var clients: Map<String, OAuth2ClientProperties> = mapOf(),
)

data class OAuth2ClientProperties(
    var registrationId: String? = null,
    var clientId: String = "",
    var clientSecret: String = "",
    var tokenEndpointUrl: String = "",
    var scope: String = "",
)
