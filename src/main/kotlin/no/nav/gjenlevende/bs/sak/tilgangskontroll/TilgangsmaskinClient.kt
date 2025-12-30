package no.nav.gjenlevende.bs.sak.tilgangskontroll

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.gjenlevende.bs.sak.felles.OAuth2RestOperationsFactory
import no.nav.gjenlevende.bs.sak.texas.TexasClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class TilgangsmaskinClient(
    @Value("\${tilgangsmaskin.url}") private val tilgangsmaskinUrl: URI,
    @Value("\${tilgangsmaskin.oauth.registration-id}") private val registrationId: String,
    @Value("\${tilgangsmaskin.oauth.scope}") private val tilgangsmaskinScope: String,
    private val texasClient: TexasClient,
    oauth2RestFactory: OAuth2RestOperationsFactory,
) {
    private val logger = LoggerFactory.getLogger(TilgangsmaskinClient::class.java)
    private val restTemplate: RestOperations = oauth2RestFactory.create(registrationId)
    private val objectMapper = jacksonObjectMapper()

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

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }

        return try {
            restTemplate.exchange<String>(
                url = uri,
                method = HttpMethod.GET,
                requestEntity = HttpEntity<Any>(headers),
            )
            EnkelTilgangsResponse(
                navIdent = navIdent,
                personident = personident,
                harTilgang = true,
            )
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

    private fun parseFeilRespons(responseBody: String): TilgangsmaskinFeilRespons? = runCatching { objectMapper.readValue(responseBody, TilgangsmaskinFeilRespons::class.java) }.getOrNull()

    fun sjekkAnsatt(navIdent: String): AnsattInfoResponse {
        val uri =
            UriComponentsBuilder
                .fromUri(tilgangsmaskinUrl)
                .pathSegment("dev", "ansatt", navIdent)
                .build()
                .toUri()

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }

        return try {
            val response =
                restTemplate.exchange<AnsattInfoResponse>(
                    url = uri,
                    method = HttpMethod.GET,
                    requestEntity = HttpEntity<Any>(headers),
                )
            response.body ?: throw TilgangsmaskinException("Ingen respons fra tilgangsmaskinen")
        } catch (e: Exception) {
            logger.error("Feil ved henting av ansattinfo fra tilgangsmaskinen: ${e.message}")
            throw TilgangsmaskinException("Feil ved henting av ansattinfo: ${e.message}", e)
        }
    }

    fun sjekkTilgangBulk(
        brukerToken: String,
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
                brukerToken = brukerToken,
                targetAudience = tilgangsmaskinScope,
            )

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                setBearerAuth(oboToken)
            }

        return try {
            val response =
                RestTemplate().exchange<BulkTilgangsResponse>(
                    url = uri,
                    method = HttpMethod.POST,
                    requestEntity = HttpEntity(personidenter.toSet(), headers),
                )
            response.body ?: throw TilgangsmaskinException("Ingen respons fra tilgangsmaskinen (bulk)")
        } catch (e: Exception) {
            logger.error("Feil ved bulk-sjekk av tilgang mot tilgangsmaskinen: ${e.message}")
            throw TilgangsmaskinException("Feil ved bulk-tilgangssjekk: ${e.message}", e)
        }
    }
}

class TilgangsmaskinException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
