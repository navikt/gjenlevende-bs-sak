package no.nav.gjenlevende.bs.sak.oppgave

import no.nav.gjenlevende.bs.sak.felles.OAuth2RestOperationsFactory
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class OppgaveClient(
    @Value("\${OPPGAVE_URL}")
    private val oppgaveUrl: URI,
    @Value("\${oppgave.oauth.registration-id}")
    private val registrationId: String,
    oauth2RestFactory: OAuth2RestOperationsFactory,
) {
    private val logger = LoggerFactory.getLogger(OppgaveClient::class.java)

    private val restTemplate: RestOperations = oauth2RestFactory.create(registrationId)

    fun opprettOppgave(oppgave: Oppgave) : Oppgave{
        logger.info("Lag oppgave=${oppgave}")
        val uri = lagBehandleSakOppgaveURI()
        logger.info("Sender opprettOppgave request til Oppgave-service ")

        try {
            val exchange = restTemplate.exchange(
               uri,
                HttpMethod.POST,
                HttpEntity(oppgave, httpHeaders()),
                object : ParameterizedTypeReference<Oppgave>() {}
            )

            val oppgaveResponse : Oppgave =
                exchange.body
                    ?: throw RuntimeException("Ikke body i response fra oppgave")

            require(oppgaveResponse.id != null && oppgaveResponse.id > 0) {
                "Oppgave id mangler i response fra oppgave"
            }

            return oppgaveResponse

        }catch (ex: Exception){
            throw RuntimeException("Feil ved oppretting av oppgave for behandling=${oppgave}", ex)
        }

    }

    private fun lagBehandleSakOppgaveURI(): URI = UriComponentsBuilder
        .fromUri(oppgaveUrl)
        .path("/api/v1/oppgaver")
        .build()
        .toUri()


}

private fun httpHeaders(): HttpHeaders =
    HttpHeaders().apply {
        add("X-Correlation-ID", MDC.get("callId"))
    }
