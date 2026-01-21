package no.nav.gjenlevende.bs.sak.oppgave

import no.nav.gjenlevende.bs.sak.texas.TexasClient
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URI
import java.time.Duration

@Component
class OppgaveClient(
    private val webClient: WebClient,
    private val texasClient: TexasClient,

    @Value("\${OPPGAVE_URL}")
    private val oppgaveUrl: URI,
    @Value("\${OPPGAVE_SCOPE}")
    private val oppgaveScope: URI,

) {
    private val logger = LoggerFactory.getLogger(OppgaveClient::class.java)

    companion object {
        private const val TIMEOUT_SEKUNDER = 10L
        private const val API_BASE_URL = "/api/v1/oppgaver"
    }

    fun opprettOppgave(oppgave: Oppgave) : Oppgave{
        logger.info("Lag oppgave=${oppgave}")
        val uri = lagBehandleSakOppgaveURI()
        logger.info("Sender opprettOppgave request til Oppgave-service ")
        val maskinToken = texasClient.hentMaskinToken(oppgaveScope.toString())



        return webClient
            .post()
            .uri(uri)
            .header( "Authorization", "Bearer $maskinToken")
            .header("X-Correlation-ID", MDC.get("callId") ?: "test")
            .bodyValue(oppgave)
            .retrieve()
            .bodyToMono<Oppgave>()
            .switchIfEmpty(Mono.error(NoSuchElementException("Tom respons fra oppgave")))
            .timeout(Duration.ofSeconds(TIMEOUT_SEKUNDER))
            .doOnNext { response ->
                logger.info("Oppgave opprettet med id: ${response.id} ")
            }.doOnError {
                logger.error("Feil: å hente perioder for person: $it")
            }.block() ?: throw RuntimeException("Klarte ikke opprette oppgave")

    }

    private fun lagBehandleSakOppgaveURI(): URI = UriComponentsBuilder
        .fromUri(oppgaveUrl)
        .path(API_BASE_URL)
        .build()
        .toUri()


}
