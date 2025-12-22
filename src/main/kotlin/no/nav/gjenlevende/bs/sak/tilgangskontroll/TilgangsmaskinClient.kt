package no.nav.gjenlevende.bs.sak.tilgangskontroll

import no.nav.gjenlevende.bs.sak.felles.OAuth2RestOperationsFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.client.exchange
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class TilgangsmaskinClient(
    @Value("\${tilgangsmaskin.url}") private val tilgangsmaskinUrl: URI,
    @Value("\${tilgangsmaskin.oauth.registration-id}") registrationId: String,
    oauth2RestFactory: OAuth2RestOperationsFactory,
) {
    private val logger = LoggerFactory.getLogger(TilgangsmaskinClient::class.java)
    private val restTemplate: RestOperations = oauth2RestFactory.create(registrationId)

    fun sjekkTilgangEnkel(
        navIdent: String,
        personident: String,
        regelType: RegelType = RegelType.KOMPLETT_REGELTYPE,
    ): EnkelTilgangsResponse {
        val regelPath =
            when (regelType) {
                RegelType.KJERNE_REGELTYPE -> "kjerne"
                else -> "komplett"
            }

        val uri =
            UriComponentsBuilder
                .fromUri(tilgangsmaskinUrl)
                .pathSegment("dev", regelPath, navIdent, personident)
                .build()
                .toUri()

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        return try {
            val response =
                restTemplate.exchange<String>(
                    url = uri,
                    method = HttpMethod.GET,
                    requestEntity = HttpEntity<Any>(headers),
                )

            when (response.statusCode.value()) {
                204 -> {
                    EnkelTilgangsResponse(
                        navIdent = navIdent,
                        personident = personident,
                        harTilgang = true,
                        avvisningsgrunn = null,
                        begrunnelse = null,
                    )
                }

                else -> {
                    EnkelTilgangsResponse(
                        navIdent = navIdent,
                        personident = personident,
                        harTilgang = false,
                        avvisningsgrunn = "UKJENT",
                        begrunnelse = response.body,
                    )
                }
            }
        } catch (e: org.springframework.web.client.HttpClientErrorException.Forbidden) {
            logger.info("Tilgang avvist for ansatt $navIdent til bruker $personident: ${e.responseBodyAsString}")
            EnkelTilgangsResponse(
                navIdent = navIdent,
                personident = personident,
                harTilgang = false,
                avvisningsgrunn = parseAvvisningsgrunn(e.responseBodyAsString),
                begrunnelse = parseBegrunnelse(e.responseBodyAsString),
            )
        } catch (e: Exception) {
            logger.error("Feil ved sjekk av tilgang mot tilgangsmaskinen: $e")
            throw TilgangsmaskinException("Feil ved tilgangssjekk: ${e.message}", e)
        }
    }

    private fun parseAvvisningsgrunn(responseBody: String): String? =
        try {
            val regex = """"title"\s*:\s*"([^"]+)"""".toRegex()
            regex.find(responseBody)?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }

    private fun parseBegrunnelse(responseBody: String): String? =
        try {
            val regex = """"begrunnelse"\s*:\s*"([^"]+)"""".toRegex()
            regex.find(responseBody)?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }

    fun sjekkAnsatt(navIdent: String): AnsattInfoResponse {
        val uri =
            UriComponentsBuilder
                .fromUri(tilgangsmaskinUrl)
                .pathSegment("dev", "ansatt", navIdent)
                .build()
                .toUri()

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        return try {
            val response =
                restTemplate.exchange<AnsattInfoResponse>(
                    url = uri,
                    method = HttpMethod.GET,
                    requestEntity = HttpEntity<Any>(headers),
                )
            response.body ?: throw TilgangsmaskinException("Ingen respons fra tilgangsmaskinen")
        } catch (e: Exception) {
            logger.error("Feil ved henting av ansattinfo fra tilgangsmaskinen: $e")
            throw TilgangsmaskinException("Feil ved henting av ansattinfo: ${e.message}", e)
        }
    }

    fun sjekkTilgangBulk(
        navIdent: String,
        personidenter: List<String>,
        regelType: RegelType = RegelType.KOMPLETT_REGELTYPE,
    ): BulkTilgangsResponse {
        val uri =
            UriComponentsBuilder
                .fromUri(tilgangsmaskinUrl)
                .pathSegment("api", "v1", "bulk", "obo", regelType.name)
                .build()
                .toUri()

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

        val request = BulkTilgangsRequest(personidenter)
        val entity = HttpEntity(request, headers)

        return try {
            val response =
                restTemplate.exchange<BulkTilgangsResponse>(
                    url = uri,
                    method = HttpMethod.POST,
                    requestEntity = entity,
                )
            response.body ?: throw TilgangsmaskinException("Ingen respons fra tilgangsmaskinen (bulk)")
        } catch (e: Exception) {
            logger.error("Feil ved bulk-sjekk av tilgang mot tilgangsmaskinen: $e")
            throw TilgangsmaskinException("Feil ved bulk-tilgangssjekk: ${e.message}", e)
        }
    }
}

class TilgangsmaskinException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
