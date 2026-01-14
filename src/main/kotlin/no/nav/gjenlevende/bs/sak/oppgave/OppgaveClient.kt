package no.nav.gjenlevende.bs.sak.oppgave

import io.micrometer.core.instrument.Metrics
import no.nav.gjenlevende.bs.sak.felles.OAuth2RestOperationsFactory
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import kotlin.math.min

@Component
class OppgaveClient(
    @Value("\${OPPGAVE_URL}") private val pdlUrl: URI,
    @Value("\${pdl.oauth.registration-id}") registrationId: String,
    oauth2RestFactory: OAuth2RestOperationsFactory,
) {
    private val logger = LoggerFactory.getLogger(OppgaveClient::class.java)

    private val restTemplate: RestOperations = oauth2RestFactory.create(registrationId)

    fun opprettOppgave(oppgaveRequest: OppgaveRequest): Long {
        logger.info("Oppretting av oppgave er ikke implementert ennå for behandling=${oppgaveRequest.behandlingsId}")
        throw NotImplementedError("OppgaveClient.opprettOppgave er ikke implementert ennå")
        // restTemplate.postForEntity<>()
    }
}
