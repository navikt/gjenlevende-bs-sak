package no.nav.gjenlevende.bs.sak.service

import no.nav.gjenlevende.bs.sak.dto.PersonPerioderResponse
import no.nav.gjenlevende.bs.sak.dto.PersonidentRequest
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

    fun hentPerioderForPerson(
        brukerToken: String,
        personident: String,
    ): Mono<PersonPerioderResponse> {
        val oboToken =
            texasClient.hentOboToken(
                brukerToken = brukerToken,
                targetAudience = gjenlevendeBsInfotrygdAudience,
            )

        return infotrygdWebClient
            .post()
            .uri("$API_BASE_URL/perioder")
            .header("Authorization", "Bearer $oboToken")
            .bodyValue(PersonidentRequest(personident = personident))
            .retrieve()
            .bodyToMono<PersonPerioderResponse>()
            .timeout(Duration.ofSeconds(TIMEOUT_SEKUNDER))
            .doOnSuccess { response ->
                logger.info("Hentet perioder for person: ${response.barnetilsyn.size} barnetilsyn, ${response.skolepenger.size} skolepenger")
            }.doOnError { logger.error("Feilet å hente perioder for person: $it") }
    }

    fun hentPerioderForPersonSync(
        brukerToken: String,
        personident: String,
    ): PersonPerioderResponse =
        hentPerioderForPerson(
            brukerToken = brukerToken,
            personident = personident,
        ).block() ?: throw RuntimeException("Klarte ikke å hente perioder for person fra gjenlevende-bs-infotrygd")
}
