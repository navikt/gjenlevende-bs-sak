package no.nav.gjenlevende.bs.sak.oppgave

import no.nav.gjenlevende.bs.sak.felles.OAuth2RestOperationsFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class OppgaveClient(
    @Value("\${OPPGAVE_URL}") private val oppgaveUrl: URI,
    @Value("\${oppgave.oauth.registration-id}") private val registrationId: String,
    oauth2RestFactory: OAuth2RestOperationsFactory,
) {
    private val logger = LoggerFactory.getLogger(OppgaveClient::class.java)

    //private val restTemplate: RestOperations = oauth2RestFactory.create(registrationId)

    fun opprettOppgave(oppgaveRequest: OppgaveRequest): Long {
        logger.info("Oppretting av oppgave er ikke implementert ennå og derfor ikke opprettet for behandling=${oppgaveRequest.behandlingsId}")
        throw NotImplementedError("OppgaveClient.opprettOppgave er ikke implementert ennå $oppgaveUrl $registrationId ")
        // restTemplate.postForEntity<>()
    }
}
