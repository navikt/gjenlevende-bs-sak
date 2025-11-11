package no.nav.gjenlevende.bs.sak.service

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
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
        logger.info("Henter OBO token fra Texas. Endpoint: $tokenExchangeEndpoint, scope: $targetAudience")

        return try {
            val response =
                webClient
                    .post()
                    .uri("$tokenExchangeEndpoint?scope=$targetAudience")
                    .header("Authorization", "Bearer $brukerToken")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .retrieve()
                    .bodyToMono<TexasOboResponse>()
                    .block()

            response?.accessToken
                ?: throw RuntimeException("Texas returnerte tomt access_token")
        } catch (e: WebClientResponseException) {
            logger.error(
                "Texas API feilet med status ${e.statusCode}. " +
                    "Response body: ${e.responseBodyAsString}. " +
                    "Request URL: $tokenExchangeEndpoint?scope=$targetAudience",
                e,
            )
            throw RuntimeException("Kunne ikke bytte token via Texas OBO: HTTP ${e.statusCode}", e)
        } catch (e: Exception) {
            logger.error("Uventet feil ved henting av OBO token fra Texas: ${e.message}", e)
            throw RuntimeException("Kunne ikke bytte token via Texas OBO", e)
        }
    }
}

data class TexasOboResponse(
    @JsonProperty("access_token")
    val accessToken: String,
)
