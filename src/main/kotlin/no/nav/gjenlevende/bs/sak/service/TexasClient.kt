package no.nav.gjenlevende.bs.sak.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class TexasClient(
    @Value("\${NAIS_TOKEN_EXCHANGE_ENDPOINT:http://localhost:7575/obo}")
    private val tokenExchangeEndpoint: String,
) {
    private val logger = LoggerFactory.getLogger(TexasClient::class.java)

    private val webClient =
        WebClient
            .builder()
            .build()

    fun hentOboToken(
        brukerToken: String,
        targetAudience: String,
    ): String {
        logger.debug("Henter OBO token fra Texas med scope: $targetAudience")

        return try {
            val response =
                webClient
                    .post()
                    .uri("$tokenExchangeEndpoint?scope=$targetAudience")
                    .header("Authorization", "Bearer $brukerToken")
                    .retrieve()
                    .bodyToMono<TexasOboResponse>()
                    .block()

            response?.accessToken
                ?: throw RuntimeException("Texas returnerte tomt access_token")
        } catch (e: Exception) {
            logger.error("Feilet å hente OBO token fra Texas: ${e.message}", e)
            throw RuntimeException("Kunne ikke bytte token via Texas OBO", e)
        }
    }
}

/**
 * Response fra Texas OBO endpoint.
 */
data class TexasOboResponse(
    val accessToken: String,
)
