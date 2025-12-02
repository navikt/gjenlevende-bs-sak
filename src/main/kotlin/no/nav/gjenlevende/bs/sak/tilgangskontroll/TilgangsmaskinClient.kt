package no.nav.gjenlevende.bs.sak.tilgangskontroll

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.gjenlevende.bs.sak.texas.TexasClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class TilgangsmaskinClient(
    @Value("\${tilgangsmaskin.url:http://populasjonstilgangskontroll.tilgangsmaskin}")
    private val tilgangsmaskinUrl: String,
    @Value("\${tilgangsmaskin.scope:api://dev-gcp.tilgangsmaskin.populasjonstilgangskontroll/.default}")
    private val tilgangsmaskinScope: String,
    private val texasClient: TexasClient,
) {
    private val logger = LoggerFactory.getLogger(TilgangsmaskinClient::class.java)

    private val webClient =
        WebClient
            .builder()
            .baseUrl(tilgangsmaskinUrl)
            .build()

    /**
     * Sjekker om ansatt har tilgang til en bruker ved å bruke bulk OBO endpoint.
     * Returnerer true hvis ansatt har tilgang (status 204), false ellers.
     */
    fun harTilgangTilBruker(fnr: String): Boolean {
        logger.info("Sjekker tilgang til bruker via tilgangsmaskin bulk OBO endpoint")

        val brukerToken = hentBrukerToken()
        val oboToken = texasClient.hentOboToken(brukerToken, tilgangsmaskinScope)

        return try {
            val response =
                webClient
                    .post()
                    .uri("/api/v1/bulk/obo/KOMPLETT_REGELTYPE")
                    .header("Authorization", "Bearer $oboToken")
                    .header("Content-Type", "application/json")
                    .bodyValue(setOf(TilgangsmaskinBulkRequest(fnr)))
                    .retrieve()
                    .bodyToMono<TilgangsmaskinBulkResponse>()
                    .block()

            val resultat = response?.resultater?.firstOrNull { it.brukerId == fnr }

            when {
                resultat == null -> {
                    logger.warn("Ingen resultat funnet for bruker i bulk response")
                    false
                }

                resultat.status == HttpStatus.NO_CONTENT.value() -> {
                    logger.info("Tilgang godkjent for bruker")
                    true
                }

                else -> {
                    logger.info("Tilgang avvist for bruker med status: ${resultat.status}")
                    false
                }
            }
        } catch (e: WebClientResponseException) {
            logger.error(
                "Tilgangsmaskin bulk request feilet med status ${e.statusCode}: ${e.responseBodyAsString}",
                e,
            )
            false
        } catch (e: Exception) {
            logger.error("Uventet feil ved sjekk av tilgang via tilgangsmaskin: ${e.message}", e)
            false
        }
    }

    /**
     * Sjekker tilgang for flere brukere samtidig.
     * Returnerer liste med fnr for brukere ansatt har tilgang til.
     */
    fun harTilgangTilBrukere(fnrList: List<String>): List<String> {
        if (fnrList.isEmpty()) {
            return emptyList()
        }

        if (fnrList.size > 1000) {
            throw IllegalArgumentException("Kan ikke sjekke tilgang for mer enn 1000 brukere samtidig")
        }

        logger.info("Sjekker tilgang til ${fnrList.size} brukere via tilgangsmaskin bulk OBO endpoint")

        val brukerToken = hentBrukerToken()
        val oboToken = texasClient.hentOboToken(brukerToken, tilgangsmaskinScope)

        return try {
            val response =
                webClient
                    .post()
                    .uri("/api/v1/bulk/obo/KOMPLETT_REGELTYPE")
                    .header("Authorization", "Bearer $oboToken")
                    .header("Content-Type", "application/json")
                    .bodyValue(fnrList.map { TilgangsmaskinBulkRequest(it) }.toSet())
                    .retrieve()
                    .bodyToMono<TilgangsmaskinBulkResponse>()
                    .block()

            response
                ?.resultater
                ?.filter { it.status == HttpStatus.NO_CONTENT.value() }
                ?.map { it.brukerId }
                ?: emptyList()
        } catch (e: WebClientResponseException) {
            logger.error(
                "Tilgangsmaskin bulk request feilet med status ${e.statusCode}: ${e.responseBodyAsString}",
                e,
            )
            emptyList()
        } catch (e: Exception) {
            logger.error("Uventet feil ved sjekk av tilgang via tilgangsmaskin: ${e.message}", e)
            emptyList()
        }
    }

    private fun hentBrukerToken(): String {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication !is JwtAuthenticationToken) {
            throw IllegalStateException("Ingen JWT token funnet i security context")
        }

        return authentication.token.tokenValue
    }
}

data class TilgangsmaskinBulkRequest(
    @JsonProperty("brukerId")
    val brukerId: String,
)

data class TilgangsmaskinBulkResponse(
    @JsonProperty("resultater")
    val resultater: List<TilgangsmaskinBulkResultat>,
)

data class TilgangsmaskinBulkResultat(
    @JsonProperty("brukerId")
    val brukerId: String,
    @JsonProperty("status")
    val status: Int,
)
