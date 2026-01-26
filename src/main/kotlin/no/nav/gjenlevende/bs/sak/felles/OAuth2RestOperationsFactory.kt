package no.nav.gjenlevende.bs.sak.felles

import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate

@Component
class OAuth2RestOperationsFactory(
    private val authorizedClientManager: OAuth2AuthorizedClientManager,
) {
    fun create(
        registrationId: String,
        principalName: String = "application",
    ): RestOperations {
        val restTemplate = RestTemplate()
        // restTemplate.interceptors.add(BearerTokenInterceptor(authorizedClientManager, registrationId, principalName))
        return restTemplate
    }
}
