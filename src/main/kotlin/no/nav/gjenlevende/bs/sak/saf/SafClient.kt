package no.nav.gjenlevende.bs.sak.saf

import no.nav.gjenlevende.bs.sak.config.SafConfig
import no.nav.gjenlevende.bs.sak.texas.TexasClient
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.UUID

@Service
class SafClient(
    val safConfig: SafConfig,
    private val texasClient: TexasClient,
) {
    private val logger = LoggerFactory.getLogger(SafClient::class.java)

    val safWebClient =
        WebClient
            .builder()
            .baseUrl(safConfig.safUri.toString())
            .defaultHeader("Content-Type", "application/json")
            .build()

    fun utførQuery(
        query: String,
        variables: JournalposterForBrukerRequest,
        operasjon: String,
    ): SafJournalpostBrukerData {
        val request =
            SafJournalpostRequest(
                query = query,
                variables = variables.tilSafRequestForBruker(),
            )

        logger.info("Utfører SAF-operasjon: $operasjon")

        return try {
            val response =
                safWebClient
                    .post()
                    .headers { it.addAll(lagSafHeaders()) }
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono<SafJournalpostResponse>()
                    .block() ?: throw SafException("Ingen respons fra SAF for $operasjon")

            val safResponse = response

            håndterSafErrrors(safResponse.errors, operasjon)

            safResponse.data ?: throw SafException("Fant ingen person i SAF for brukerId")
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
            setBearerAuth(texasClient.hentOboToken(safConfig.safScope))
            contentType = MediaType.APPLICATION_JSON
            accept = listOf(MediaType.APPLICATION_JSON)
            add(NAV_CALL_ID, UUID.randomUUID().toString())
        }

    companion object {
        private const val NAV_CALL_ID = "Nav-Callid"
    }
}
