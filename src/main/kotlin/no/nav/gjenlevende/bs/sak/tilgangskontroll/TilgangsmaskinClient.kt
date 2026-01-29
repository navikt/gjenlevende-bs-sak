package no.nav.gjenlevende.bs.sak.tilgangskontroll

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.gjenlevende.bs.sak.texas.TexasClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class TilgangsmaskinClient(
    @Value("\${tilgangsmaskin.url}") private val tilgangsmaskinUrl: URI,
    @Value("\${tilgangsmaskin.oauth.scope}") private val tilgangsmaskinScope: String,
    private val texasClient: TexasClient,
) {
    private val logger = LoggerFactory.getLogger(TilgangsmaskinClient::class.java)

    val tilgangsmaskinRestClient =
        RestClient
            .builder()
            .baseUrl(tilgangsmaskinUrl)
            .defaultHeader("Content-Type", "application/json")
            .build()

    private val objectMapper =
        jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

    fun sjekkTilgangEnkel(
        navIdent: String,
        personident: String,
        regelType: RegelType = RegelType.KJERNE_REGELTYPE,
    ): EnkelTilgangsResponse {
        val regelPath = if (regelType == RegelType.KJERNE_REGELTYPE) "kjerne" else "komplett"

        val uri =
            UriComponentsBuilder
                .fromUri(tilgangsmaskinUrl)
                .pathSegment("dev", regelPath, navIdent, personident)
                .build()
                .toUri()

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                setBearerAuth(texasClient.hentOboToken(tilgangsmaskinScope))
            }

        return try {
            return tilgangsmaskinRestClient
                .get()
                .uri(uri)
                .headers { it.addAll(headers) }
                .retrieve()
                .body(EnkelTilgangsResponse::class.java) ?: throw TilgangsmaskinException("Ingen respons fra tilgangsmaskinen")
        } catch (e: org.springframework.web.client.HttpClientErrorException.Forbidden) {
            logger.info("Tilgang avvist for ansatt $navIdent til bruker $personident")
            val feilRespons = parseFeilRespons(e.responseBodyAsString)
            EnkelTilgangsResponse(
                navIdent = navIdent,
                personident = personident,
                harTilgang = false,
                avvisningskode = feilRespons?.title?.let { runCatching { Avvisningskode.valueOf(it) }.getOrNull() },
                begrunnelse = feilRespons?.begrunnelse,
            )
        } catch (e: Exception) {
            logger.error("Feil ved sjekk av tilgang mot tilgangsmaskinen: ${e.message}")
            throw TilgangsmaskinException("Feil ved tilgangssjekk: ${e.message}", e)
        }
    }

    private fun parseFeilRespons(responseBody: String): TilgangsmaskinFeilResponse? = runCatching { objectMapper.readValue(responseBody, TilgangsmaskinFeilResponse::class.java) }.getOrNull()

    fun sjekkAnsatt(navIdent: String): AnsattInfoResponse {
        val uri =
            UriComponentsBuilder
                .fromUri(tilgangsmaskinUrl)
                .pathSegment("dev", "ansatt", navIdent)
                .build()
                .toUri()

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }

        try {
            return tilgangsmaskinRestClient
                .get()
                .uri(uri)
                .headers { it.addAll(headers) }
                .retrieve()
                .body(AnsattInfoResponse::class.java) ?: throw TilgangsmaskinException("Ingen respons fra tilgangsmaskinen")
        } catch (e: Exception) {
            logger.error("Feil ved henting av ansattinfo fra tilgangsmaskinen: ${e.message}")
            throw TilgangsmaskinException("Feil ved henting av ansattinfo: ${e.message}", e)
        }
    }

    fun sjekkTilgangBulk(
        personidenter: List<String>,
        regelType: RegelType = RegelType.KJERNE_REGELTYPE,
    ): BulkTilgangsResponse {
        val uri =
            UriComponentsBuilder
                .fromUri(tilgangsmaskinUrl)
                .pathSegment("api", "v1", "bulk", "obo", regelType.name)
                .build()
                .toUri()

        val oboToken =
            texasClient.hentOboToken(
                targetAudience = tilgangsmaskinScope,
            )

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                setBearerAuth(oboToken)
            }

        try {
            return tilgangsmaskinRestClient
                .post()
                .uri(uri)
                .body(personidenter.toSet())
                .headers { it.addAll(headers) }
                .retrieve()
                .body(BulkTilgangsResponse::class.java) ?: throw TilgangsmaskinException("Ingen respons fra tilgangsmaskinen (bulk)")
        } catch (e: Exception) {
            logger.error("Feil ved bulk sjekk av tilgang mot tilgangsmaskinen: ${e.message}")
            throw TilgangsmaskinException("Feil ved bulk-tilgangssjekk: ${e.message}", e)
        }
    }
}

class TilgangsmaskinException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
