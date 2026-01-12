package no.nav.gjenlevende.bs.sak.opplysninger

import no.nav.gjenlevende.bs.sak.felles.OAuth2RestOperationsFactory
import no.nav.gjenlevende.bs.sak.felles.auditlogger.Tilgang
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class FamilieIntegrasjonerClient(
    @Value("\${familie-integrasjoner.url}") private val integrasjonUri: URI,
    @Value("\${familie-integrasjoner.oauth.registration-id}") registrationId: String,
    oauth2RestFactory: OAuth2RestOperationsFactory,
) {
    private val restTemplate: RestOperations = oauth2RestFactory.create(registrationId)

    val tilgangRelasjonerUri: URI =
        UriComponentsBuilder
            .fromUri(integrasjonUri)
            .pathSegment(PATH_TILGANG_RELASJONER)
            .build()
            .toUri()

    fun sjekkTilgangTilPersonMedRelasjoner(personident: String): Tilgang {
        val headers =
            HttpHeaders().also {
                it.set(HEADER_NAV_TEMA, HEADER_NAV_TEMA_EYO)
            }

        val entity =
            HttpEntity(
                no.nav.familie.kontrakter.felles
                    .PersonIdent(personident),
                headers,
            )
        return restTemplate
            .exchange<Tilgang>(
                tilgangRelasjonerUri,
                HttpMethod.POST,
                entity,
            ).body ?: error("Ingen response ved henting av tilgang til person med relasjoner")
    }

    companion object {
        private const val PATH_TILGANG_RELASJONER = "api/tilgang/person-med-relasjoner"
        private const val HEADER_NAV_TEMA = "Nav-Tema"
        private const val HEADER_NAV_TEMA_EYO = "ENF" // TODO Gjør om til EYO og tillat temaet i integrasjoner
    }
}
