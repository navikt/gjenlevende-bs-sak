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
import java.util.UUID

@Configuration
class OppgaveWebClientConfig {
    @Bean
    fun oppgaveWebClient(
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

    fun opprettOppgaveM2M(oppgaveRequest: LagOppgaveRequest): OppgaveDto {
        logger.info("Sender opprettOppgave request til Oppgave-service ")
        val maskinToken = texasClient.hentMaskinToken(oppgaveScope.toString())

        return oppgaveWebClient
            .post()
            .uri(API_BASE_URL)
            .header("Authorization", "Bearer $maskinToken")
            .header("X-Correlation-ID", MDC.get("callId") ?: "${UUID.randomUUID()}")
            .bodyValue(oppgaveRequest)
            .retrieve()
            .bodyToMono<OppgaveDto>()
            .switchIfEmpty(Mono.error(NoSuchElementException("Tom respons fra oppgave")))
            .timeout(Duration.ofSeconds(TIMEOUT_SEKUNDER))
            .doOnNext { response ->
                logger.info("Oppgave opprettet med id: ${response.id} ")
            }.doOnError {
                logger.error("Feil: klarte ikke opprette oppgave med $oppgaveRequest")
            }.block() ?: throw RuntimeException("Klarte ikke opprette oppgave")
    }

    // TODO: Dette må kanskje gjøres med OBO, vi ser på dette siden.
    fun hentOppgaveM2M(oppgaveId: Long): OppgaveDto {
        logger.info("Henter oppgave med id=$oppgaveId fra Oppgave-service")
        val maskinToken = texasClient.hentMaskinToken(oppgaveScope.toString())

        return oppgaveWebClient
            .get()
            .uri("$API_BASE_URL/$oppgaveId")
            .header("Authorization", "Bearer $maskinToken")
            .header("X-Correlation-ID", MDC.get("callId") ?: "${UUID.randomUUID()}")
            .retrieve()
            .bodyToMono<OppgaveDto>()
            .switchIfEmpty(Mono.error(NoSuchElementException("Tom respons fra oppgave")))
            .timeout(Duration.ofSeconds(TIMEOUT_SEKUNDER))
            .doOnNext { response ->
                logger.info("Hentet oppgave med id: ${response.id}")
            }.doOnError {
                logger.error("Feil: klarte ikke hente oppgave med id=$oppgaveId")
            }.block() ?: throw RuntimeException("Klarte ikke hente oppgave med id=$oppgaveId")
    }
}

data class LagOppgaveRequest(
    val personident: String,
    val saksreferanse: String,
    val prioritet: OppgavePrioritet = OppgavePrioritet.NORM,
    val tema: Tema,
    val behandlingstema: String? = null,
    val behandlingstype: String? = null,
    val fristFerdigstillelse: String,
    val aktivDato: String,
    val oppgavetype: OppgavetypeEYO,
    val beskrivelse: String,
    val tilordnetRessurs: String,
    val behandlesAvApplikasjon: String,
    val tildeltEnhetsnr: String,
)
