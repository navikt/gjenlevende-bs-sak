package no.nav.gjenlevende.bs.sak.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class InfotrygdClient(
    private val infotrygdWebClient: WebClient,
) {
    private val logger = LoggerFactory.getLogger(InfotrygdClient::class.java)

    companion object {
        private const val TIMEOUT_SEKUNDER = 10L
        private const val API_BASE_URL = "/api/infotrygd"
    }

    fun ping(bearerToken: String): Mono<String> =
        infotrygdWebClient
            .get()
            .uri("$API_BASE_URL/ping")
            .header("Authorization", "Bearer $bearerToken")
            .retrieve()
            .bodyToMono<String>()
            .timeout(Duration.ofSeconds(TIMEOUT_SEKUNDER))
            .doOnSuccess { logger.info("Klarte å pinge gjenlevende-bs-infotrygd med melding: {}", it) }
            .doOnError { logger.error("Feilet å pinge gjenlevende-bs-infotrygd med melding: ", it) }

    fun pingSync(bearerToken: String): String = ping(bearerToken).block() ?: throw RuntimeException("Klarte ikke å pinge gjenlevene-bs-infotrygd")
}
