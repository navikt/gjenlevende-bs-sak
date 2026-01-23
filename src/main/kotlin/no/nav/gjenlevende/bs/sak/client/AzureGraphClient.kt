package no.nav.gjenlevende.bs.sak.client

import no.nav.gjenlevende.bs.sak.client.domain.AzureAdBruker
import no.nav.gjenlevende.bs.sak.client.domain.AzureAdBrukere
import no.nav.gjenlevende.bs.sak.felles.OAuth2RestOperationsFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class AzureGraphClient(
    @Value("\${AAD_GRAPH_API_URI}") private val aadGraphURI: URI,
    @Value("\${AZURE_APP_CLIENT_ID}") clientId: String,
    oauth2RestFactory: OAuth2RestOperationsFactory,
) {
    private val restTemplate: RestOperations = oauth2RestFactory.create(clientId)

    private fun saksbehandlerUri(id: String): URI =
        UriComponentsBuilder
            .fromUri(aadGraphURI)
            .pathSegment(USERS, id)
            .queryParam("\$select", FELTER)
            .build()
            .toUri()

    private fun saksbehandlersokUri(navIdent: String): URI =
        UriComponentsBuilder
            .fromUri(aadGraphURI)
            .pathSegment(USERS)
            .queryParam("\$search", "\"onPremisesSamAccountName:$navIdent\"")
            .queryParam("\$select", FELTER)
            .build()
            .toUri()

    fun finnSaksbehandler(navIdent: String): AzureAdBrukere =
        try {
            val headers =
                HttpHeaders().apply {
                    add("ConsistencyLevel", "eventual")
                    accept = listOf(MediaType.APPLICATION_JSON)
                }

            restTemplate
                .exchange<AzureAdBrukere>(
                    saksbehandlersokUri(navIdent),
                    HttpMethod.GET,
                    HttpEntity<Void>(headers),
                ).body
                ?: error("Tom response ved søk etter saksbehandler")
        } catch (e: Exception) {
            throw OppslagException(
                "Feil ved henting av saksbehandler med nav ident",
            )
        }

    fun hentSaksbehandler(id: String): AzureAdBruker =
        try {
            val headers =
                HttpHeaders().apply {
                    accept = listOf(MediaType.APPLICATION_JSON)
                }

            restTemplate
                .exchange<AzureAdBruker>(
                    saksbehandlerUri(id),
                    HttpMethod.GET,
                    HttpEntity<Void>(headers),
                ).body
                ?: error("Tom response ved henting av saksbehandler")
        } catch (e: Exception) {
            throw OppslagException(
                "Feil ved henting av saksbehandler med id",
            )
        }

    companion object {
        private const val USERS = "users"
        private const val FELTER = "givenName,surname,onPremisesSamAccountName,id,userPrincipalName,streetAddress,city"
    }

    class OppslagException(
        message: String,
        cause: Throwable? = null,
    ) : RuntimeException(message, cause)
}
