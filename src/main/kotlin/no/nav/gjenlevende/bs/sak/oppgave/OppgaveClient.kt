package no.nav.gjenlevende.bs.sak.oppgave

import no.nav.gjenlevende.bs.sak.texas.TexasClient
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.net.URI
import java.time.Duration

@Configuration
class OppgaveWebClientConfig {
    @Bean
    open fun oppgaveWebClient(
        @Value("\${OPPGAVE_URL}")
        oppgaveUrl: String,
    ): WebClient =
        WebClient
            .builder()
            .baseUrl(oppgaveUrl)
            .defaultHeader("Content-Type", "application/json")
            .build()
}

@Component
class OppgaveClient(
    private val oppgaveWebClient: WebClient,
    private val texasClient: TexasClient,
    @Value("\${OPPGAVE_SCOPE}")
    private val oppgaveScope: URI,
) {
    private val logger = LoggerFactory.getLogger(OppgaveClient::class.java)

    companion object {
        private const val TIMEOUT_SEKUNDER = 10L
        private const val API_BASE_URL = "/api/v1/oppgaver"
    }

    fun opprettOppgaveOBO(
        oppgave: LagOppgaveRequest,
    ): Mono<Oppgave> {
        logger.info("Lag oppgave=$oppgave")

        val obo = texasClient.hentOboToken(oppgaveScope.toString())


        return oppgaveWebClient
            .post()
            .uri(API_BASE_URL)
            .headers() { headers ->
                headers.setBearerAuth(obo)
                headers.set("X-Correlation-ID", MDC.get("callId") ?: "test-gjenlevende-bs-sak")
            }
            .bodyValue(oppgave)
            .retrieve()
            .bodyToMono<Oppgave>()
            .timeout(Duration.ofSeconds(TIMEOUT_SEKUNDER))
    }

    fun opprettOppgaveM2M(oppgave: Oppgave): Oppgave {
        logger.info("Lag oppgave=$oppgave")
        // val uri = lagBehandleSakOppgaveURI()
        logger.info("Sender opprettOppgave request til Oppgave-service ")
        val maskinToken = texasClient.hentMaskinToken(oppgaveScope.toString())

        return oppgaveWebClient
            .post()
            .uri(API_BASE_URL)
            .header("Authorization", "Bearer $maskinToken")
            .header("X-Correlation-ID", MDC.get("callId") ?: "test-gjenlevende-bs-sak") // TODO fix callId for m2m
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

//    private fun lagBehandleSakOppgaveURI(): URI =
//        UriComponentsBuilder
//            .fromUri(oppgaveUrl)
//            .path(API_BASE_URL)
//            .build()
//            .toUri()
}
