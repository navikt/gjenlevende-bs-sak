package no.nav.gjenlevende.bs.sak.service

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
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
        logger.info("Henter OBO token fra Texas. Endpoint: $tokenExchangeEndpoint, target: $targetAudience")

        val response =
            try {
                val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
                formData.add("identity_provider", "azuread")
                formData.add("target", targetAudience)
                formData.add("user_token", brukerToken)

                webClient
                    .post()
                    .uri(tokenExchangeEndpoint)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono<TexasOboResponse>()
                    .block()
            } catch (e: WebClientResponseException) {
                logger.error("Texas API feilet med status ${e.statusCode} og response body: ${e.responseBodyAsString}")
                throw RuntimeException("Kunne ikke bytte token via Texas OBO: HTTP ${e.statusCode}", e)
            } catch (e: Exception) {
                logger.error("Uventet feil ved henting av OBO token fra Texas: ${e.message}", e)
                throw RuntimeException("Kunne ikke bytte token via Texas OBO", e)
            }

        val token = response?.accessToken
        if (token.isNullOrBlank()) {
            throw RuntimeException("Texas returnerte tomt access_token")
        }

        return token
    }
}

data class TexasOboResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("expires_in")
    val utløperOm: Int,
    @JsonProperty("token_type")
    val tokenType: String,
)
