package no.nav.gjenlevende.bs.sak.pdl

import no.nav.gjenlevende.bs.sak.config.PdlConfig
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations

@Service
class PdlClient(
    val pdlConfig: PdlConfig,
    @Qualifier("azureClientCredential") private val restTemplate: RestOperations,
) {
    private val logger = LoggerFactory.getLogger(PdlClient::class.java)

    fun hentNavn(ident: String): Navn? {
        val request =
            PdlRequest(
                query = PdlConfig.hentNavnQuery,
                variables = mapOf("ident" to ident),
            )

        val headers =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("Tema", "EYO")
                set("behandlingsnummer", "B373")
            }

        val entity = HttpEntity(request, headers)

        logger.info("Henter navn fra PDL for person")

        return try {
            val response =
                restTemplate.exchange(
                    pdlConfig.pdlUri,
                    HttpMethod.POST,
                    entity,
                    object : ParameterizedTypeReference<PdlResponse<HentPersonData>>() {},
                )

            val pdlResponse =
                response.body
                    ?: throw PdlException("Ingen respons fra PDL")

            if (pdlResponse.errors != null && pdlResponse.errors.isNotEmpty()) {
                logger.error("Feil fra PDL: ${pdlResponse.errors}")
                throw PdlException("Feil ved henting av navn fra PDL: ${pdlResponse.errors.firstOrNull()?.message}")
            }

            val hentPerson =
                pdlResponse.data?.hentPerson
                    ?: throw PdlException("Fant ingen person i PDL")

            val navnListe = hentPerson.navn
            if (navnListe.isEmpty()) {
                throw PdlException("Personen har ingen navn registrert i PDL")
            }

            navnListe.first()
        } catch (e: Exception) {
            when (e) {
                is PdlException -> {
                    throw e
                }

                else -> {
                    logger.error("Feil ved kall til PDL", e)
                    throw PdlException("Teknisk feil ved henting av navn fra PDL", e)
                }
            }
        }
    }
}

class PdlException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
