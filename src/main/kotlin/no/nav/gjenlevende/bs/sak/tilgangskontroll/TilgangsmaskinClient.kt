package no.nav.gjenlevende.bs.sak.tilgangskontroll

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.gjenlevende.bs.sak.texas.TexasClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient
import java.time.Duration

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
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create().responseTimeout(Duration.ofSeconds(10)),
                ),
            ).build()

    fun sjekkTilgang(personident: String): TilgangResultat {
        logger.info("Sjekker tilgang til bruker via tilgangsmaskin bulk OBO endpoint")

        val brukerToken = hentBrukerToken()
        val oboToken = texasClient.hentOboToken(brukerToken, tilgangsmaskinScope)

        return try {
            val response =
                webClient
                    .post()
                    .uri("/api/v1/bulk/obo/KJERNE_REGELTYPE")
                    .header("Authorization", "Bearer $oboToken")
                    .header("Content-Type", "application/json")
                    .bodyValue(setOf(personident))
                    .retrieve()
                    .bodyToMono<TilgangsmaskinBulkResponse>()
                    .block()

            val resultat = response?.resultater?.firstOrNull { it.brukerId == personident }

            when {
                resultat == null -> {
                    logger.warn("Ingen resultat funnet for bruker i bulk response")
                    TilgangResultat.avvist("Ingen resultat fra tilgangsmaskin")
                }

                resultat.status == HttpStatus.NO_CONTENT.value() -> {
                    logger.info("Tilgang godkjent for bruker")
                    TilgangResultat.godkjent()
                }

                resultat.status == HttpStatus.NOT_FOUND.value() -> {
                    logger.info("Bruker ikke funnet i PDL, antar tilgang OK")
                    TilgangResultat.godkjent()
                }

                else -> {
                    val avvisningsgrunn = Avvisningsgrunn.fraKode(resultat.detaljer?.avvisningsgrunn)
                    val begrunnelse = resultat.detaljer?.begrunnelse ?: avvisningsgrunn?.beskrivelse
                    logger.info(
                        "Tilgang avvist for bruker med status: ${resultat.status}, " +
                            "avvisningsgrunn: $avvisningsgrunn, begrunnelse: $begrunnelse",
                    )
                    if (avvisningsgrunn != null) {
                        TilgangResultat.avvist(avvisningsgrunn, begrunnelse)
                    } else {
                        TilgangResultat.avvist("Tilgang avvist")
                    }
                }
            }
        } catch (e: WebClientResponseException) {
            logger.error(
                "Tilgangsmaskin bulk request feilet med status ${e.statusCode}: ${e.responseBodyAsString} der feil er: $e",
            )
            TilgangResultat.avvist("Feil ved sjekk av tilgang")
        } catch (e: Exception) {
            logger.error("Uventet feil ved sjekk av tilgang via tilgangsmaskin: ${e.message} der feil er: $e")
            TilgangResultat.avvist("Feil ved sjekk av tilgang")
        }
    }

    fun harTilgangTilBruker(personident: String): Boolean = sjekkTilgang(personident).harTilgang

    fun harTilgangTilBrukere(personidenter: List<String>): List<String> {
        if (personidenter.isEmpty()) {
            return emptyList()
        }

        // Kommer aldri til å skje, men ja, er med for sikkerhetskyld.
        if (personidenter.size > 1000) {
            throw IllegalArgumentException("Kan ikke sjekke tilgang for mer enn 1000 brukere samtidig")
        }

        logger.info("Sjekker tilgang til ${personidenter.size} brukere via tilgangsmaskin bulk OBO endpoint")

        val brukerToken = hentBrukerToken()
        val oboToken = texasClient.hentOboToken(brukerToken, tilgangsmaskinScope)

        return try {
            val response =
                webClient
                    .post()
                    .uri("/api/v1/bulk/obo/KJERNE_REGELTYPE")
                    .header("Authorization", "Bearer $oboToken")
                    .header("Content-Type", "application/json")
                    .bodyValue(personidenter.toSet())
                    .retrieve()
                    .bodyToMono<TilgangsmaskinBulkResponse>()
                    .block()

            response
                ?.resultater
                ?.filter {
                    it.status == HttpStatus.NO_CONTENT.value() ||
                        it.status == HttpStatus.NOT_FOUND.value()
                }?.map { it.brukerId }
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

data class TilgangsmaskinBulkResponse(
    @JsonProperty("ansattId")
    val ansattId: String? = null,
    @JsonProperty("resultater")
    val resultater: Set<TilgangsmaskinBulkResultat> = emptySet(),
)

data class TilgangsmaskinBulkResultat(
    @JsonProperty("brukerId")
    val brukerId: String,
    @JsonProperty("status")
    val status: Int,
    @JsonProperty("detaljer")
    val detaljer: TilgangsmaskinDetaljer? = null,
)

data class TilgangsmaskinDetaljer(
    @JsonProperty("avvisningsgrunn")
    val avvisningsgrunn: String? = null,
    @JsonProperty("begrunnelse")
    val begrunnelse: String? = null,
)
