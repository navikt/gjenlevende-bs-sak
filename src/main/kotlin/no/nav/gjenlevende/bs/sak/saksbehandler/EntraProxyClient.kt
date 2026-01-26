package no.nav.gjenlevende.bs.sak.saksbehandler

import SaksbehandlerResponse
import no.nav.gjenlevende.bs.sak.felles.OAuth2RestOperationsFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class EntraProxyClient(
    @Value("\${ENTRA_PROXY_URL}") private val entraProxyUrl: URI,
    @Value("\${entra-proxy.oauth.registration-id}") registrationId: String,
    oauth2RestFactory: OAuth2RestOperationsFactory,
) {
    private val logger = LoggerFactory.getLogger(EntraProxyClient::class.java)
    private val restTemplate: RestOperations = oauth2RestFactory.create(registrationId)

    fun hentSaksbehandlerInfo(navIdent: String): SaksbehandlerResponse {
        val uri =
            UriComponentsBuilder
                .fromUri(entraProxyUrl)
                .pathSegment("api", "v1", "ansatt", navIdent)
                .build()
                .toUri()

        logger.info("Henter saksbehandlerinfo for navIdent: $navIdent")

        val response =
            restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                SaksbehandlerResponse::class.java,
            )

        return response.body
            ?: throw RuntimeException("Klarte ikke å hente saksbehandlerinfo fra entra-proxy")
    }
}
