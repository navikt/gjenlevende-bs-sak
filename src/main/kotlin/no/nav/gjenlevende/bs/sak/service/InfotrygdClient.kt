package no.nav.gjenlevende.bs.sak.service

import no.nav.gjenlevende.bs.sak.dto.PersonPerioderResponse
import no.nav.gjenlevende.bs.sak.dto.StønadTypeStatistikkResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class InfotrygdClient(
    private val infotrygdWebClient: WebClient,
    private val texasClient: TexasClient,
    @Value("\${gjenlevende-bs-infotrygd.audience}")
    private val gjenlevendeBsInfotrygdAudience: String,
) {
    private val logger = LoggerFactory.getLogger(InfotrygdClient::class.java)

    companion object {
        private const val TIMEOUT_SEKUNDER = 10L
        private const val API_BASE_URL = "/api/infotrygd"
    }

    fun ping(brukerToken: String): Mono<String> {
        val oboToken =
            texasClient.hentOboToken(
                brukerToken = brukerToken,
                targetAudience = gjenlevendeBsInfotrygdAudience,
            )

        return infotrygdWebClient
            .get()
            .uri("$API_BASE_URL/ping")
            .header("Authorization", "Bearer $oboToken")
            .retrieve()
            .bodyToMono<String>()
            .timeout(Duration.ofSeconds(TIMEOUT_SEKUNDER))
            .doOnSuccess { response: String? ->
                response?.let {
                    logger.info("Klarte å pinge gjenlevende-bs-infotrygd med melding: $it")
                }
            }.doOnError { logger.error("Feilet å pinge gjenlevende-bs-infotrygd med melding: $it") }
    }

    fun pingSync(brukerToken: String): String = ping(brukerToken).block() ?: throw RuntimeException("Klarte ikke å pinge gjenlevene-bs-infotrygd")

    fun hentStønadTypeStatistikk(brukerToken: String): Mono<StønadTypeStatistikkResponse> {
        val oboToken =
            texasClient.hentOboToken(
                brukerToken = brukerToken,
                targetAudience = gjenlevendeBsInfotrygdAudience,
            )

        return infotrygdWebClient
            .get()
            .uri("$API_BASE_URL/stonadtyper")
            .header("Authorization", "Bearer $oboToken")
            .retrieve()
            .bodyToMono<StønadTypeStatistikkResponse>()
            .timeout(Duration.ofSeconds(TIMEOUT_SEKUNDER))
            .doOnSuccess { response ->
                logger.info("Hentet stønadtype statistikk: ${response.totaltAntall} stønader totalt")
            }.doOnError { logger.error("Feilet å hente stønadtype statistikk: $it") }
    }

    fun hentStønadTypeStatistikkSync(brukerToken: String): StønadTypeStatistikkResponse =
        hentStønadTypeStatistikk(brukerToken).block()
            ?: throw RuntimeException("Klarte ikke å hente stønadtype statistikk fra gjenlevende-bs-infotrygd")

    fun hentPerioderForPerson(
        brukerToken: String,
        personIdent: String,
    ): Mono<PersonPerioderResponse> {
        val oboToken =
            texasClient.hentOboToken(
                brukerToken = brukerToken,
                targetAudience = gjenlevendeBsInfotrygdAudience,
            )

        return infotrygdWebClient
            .get()
            .uri("$API_BASE_URL/perioder/$personIdent")
            .header("Authorization", "Bearer $oboToken")
            .retrieve()
            .bodyToMono<PersonPerioderResponse>()
            .timeout(Duration.ofSeconds(TIMEOUT_SEKUNDER))
            .doOnSuccess { response ->
                logger.info("Hentet perioder for person: ${response.barnetilsyn.size} barnetilsyn, ${response.skolepenger.size} skolepenger")
            }.doOnError { logger.error("Feilet å hente perioder for person: $it") }
    }

    fun hentPerioderForPersonSync(
        brukerToken: String,
        personIdent: String,
    ): PersonPerioderResponse =
        hentPerioderForPerson(brukerToken, personIdent).block()
            ?: throw RuntimeException("Klarte ikke å hente perioder for person fra gjenlevende-bs-infotrygd")
}
