package no.nav.gjenlevende.bs.sak.saf

import no.nav.gjenlevende.bs.sak.config.SafConfig
import no.nav.gjenlevende.bs.sak.felles.OAuth2RestOperationsFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.util.UUID

@Service
class SafClient(
    val safConfig: SafConfig,
    @Value("\${saf.oauth.registration-id}") registrationId: String,
    oauth2RestFactory: OAuth2RestOperationsFactory,
) {
    private val logger = LoggerFactory.getLogger(SafClient::class.java)
    private val restTemplate: RestOperations = oauth2RestFactory.create(registrationId)

    fun <T> utførQuery(
        query: String,
        variables: JournalposterForBrukerRequest,
        responstype: ParameterizedTypeReference<SafJournalpostResponse<T>>,
        operasjon: String,
    ): T? {
        val request =
            SafJournalpostRequest(
                query = query,
                variables = variables.tilSafRequestForBruker(),
            )

        val headers = lagSafHeaders()
        val entity = HttpEntity(request, headers)

        logger.info("Utfører SAF-operasjon: $operasjon")

        return try {
            val response =
                restTemplate.exchange(
                    safConfig.safUri,
                    HttpMethod.POST,
                    entity,
                    responstype,
                )

            val safResponse =
                response.body
                    ?: throw SafException("Ingen respons fra SAF for $operasjon")

            håndterSafErrrors(safResponse.errors, operasjon)

            safResponse.data
        } catch (e: Exception) {
            when (e) {
                is SafException -> {
                    throw e
                }

                else -> {
                    logger.error("Teknisk feil ved SAF-operasjon: $operasjon", e)
                    throw SafException("Teknisk feil ved $operasjon", e)
                }
            }
        }
    }

    private fun håndterSafErrrors(
        errors: List<SafError>?,
        operasjon: String,
    ) {
        if (errors != null && errors.isNotEmpty()) {
            logger.error("Feil fra SAF ved $operasjon: $errors")
            val firstError = errors.firstOrNull()
            throw SafException(
                "Feil ved $operasjon: ${firstError?.message ?: "Ukjent feil"}",
            )
        }
    }

    private fun lagSafHeaders(): HttpHeaders =
        HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            accept = listOf(MediaType.APPLICATION_JSON)
            add(NAV_CALL_ID, UUID.randomUUID().toString())
        }

    companion object {
        private const val NAV_CALL_ID = "Nav-Callid"
    }
}
