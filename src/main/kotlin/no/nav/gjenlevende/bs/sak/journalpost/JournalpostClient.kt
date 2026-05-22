package no.nav.gjenlevende.bs.sak.journalpost

import no.nav.gjenlevende.bs.sak.texas.TexasClient
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.UUID

@Configuration
class JournalpostWebClientConfig {
    @Bean
    fun journalpostWebClient(
        @Value("\${SAF_URL}")
        safUrl: String,
    ): WebClient =
        WebClient
            .builder()
            .baseUrl(safUrl)
            .defaultHeader("Content-Type", "application/json")
            .build()
}

@Component
class JournalpostClient(
    private val journalpostWebClient: WebClient,
    private val texasClient: TexasClient,
    @Value("\${SAF_SCOPE}")
    private val safScope: String,
) {
    private val logger = LoggerFactory.getLogger(JournalpostClient::class.java)

    companion object {
        private const val TIMEOUT_SEKUNDER = 10L
        private const val VARIANTFORMAT = "ARKIV"
    }

    fun hentDokument(
        journalpostId: String,
        dokumentInfoId: String,
    ): ByteArray {
        logger.info("Henter dokument med journalpostId=$journalpostId og dokumentInfoId=$dokumentInfoId fra SAF")
        val oboToken = texasClient.hentOboToken(safScope)

        return journalpostWebClient
            .get()
            .uri("/rest/hentdokument/$journalpostId/$dokumentInfoId/$VARIANTFORMAT")
            .header("Authorization", "Bearer $oboToken")
            .header("Nav-Callid", MDC.get("callId") ?: "${UUID.randomUUID()}")
            .accept(MediaType.APPLICATION_PDF)
            .retrieve()
            .bodyToMono<ByteArray>()
            .switchIfEmpty(Mono.error(NoSuchElementException("Tomt svar fra SAF for journalpostId=$journalpostId")))
            .timeout(Duration.ofSeconds(TIMEOUT_SEKUNDER))
            .doOnNext { logger.info("Hentet dokument med journalpostId=$journalpostId") }
            .doOnError { logger.error("Feil ved henting av dokument med journalpostId=$journalpostId: $it") }
            .block() ?: throw RuntimeException("Klarte ikke hente dokument med journalpostId=$journalpostId")
    }
}
