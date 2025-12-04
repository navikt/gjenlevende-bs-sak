package no.nav.gjenlevende.bs.sak.saf

import no.nav.gjenlevende.bs.sak.pdl.PdlClient
import no.nav.gjenlevende.bs.sak.pdl.PdlException
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.util.UUID
import kotlin.collections.isNotEmpty

@Service
class SafClient(
    val safConfig: SafConfig,
    @Qualifier("azureClientCredential") private val restTemplate: RestOperations,
) {
    private val logger = LoggerFactory.getLogger(PdlClient::class.java)

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
                    ?: throw PdlException("Ingen respons fra SAF for $operasjon")

            // håndterPdlErrors(pdlResponse.errors, operasjon)

            safResponse.data
        } catch (e: Exception) {
            when (e) {
                is PdlException -> {
                    throw e
                }

                else -> {
                    logger.error("Teknisk feil ved SAF-operasjon: $operasjon", e)
                    throw PdlException("Teknisk feil ved $operasjon", e)
                }
            }
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

data class SafJournalposterData(
    val journalposter: List<Journalpost>?,
)

data class SafJournalpostResponse<T>(
    val data: T? = null,
    val errors: List<SafError>? = null,
) {
    fun harFeil(): Boolean = errors != null && errors.isNotEmpty()
}

data class SafError(
    val message: String,
    val extensions: SafExtension,
)

data class SafExtension(
    val code: SafErrorCode,
    val classification: String,
)

@Suppress("EnumEntryName")
enum class SafErrorCode {
    forbidden,
    not_found,
    bad_request,
    server_error,
}

data class JournalposterForBrukerRequest(
    val brukerId: Bruker,
    val antall: Int,
)

data class Bruker(
    val id: String,
    val type: BrukerIdType,
)

enum class BrukerIdType {
    AKTOERID,
    FNR,
    ORGNR,
}

fun JournalposterForBrukerRequest.tilSafRequestForBruker(): SafRequestForBruker =
    SafRequestForBruker(
        brukerId = brukerId,
        antall = antall,
    )

data class SafRequestForBruker(
    val brukerId: Bruker,
    val antall: Int,
)

data class SafJournalpostRequest(
    val variables: Any,
    val query: String,
)

fun String.graphqlCompatible(): String = StringUtils.normalizeSpace(this.replace("\n", ""))

fun graphqlQuery(path: String) = ClassPathResource(path).url.readText().graphqlCompatible()

data class Journalpost(
    val journalpostId: String? = null,
    val tema: String? = null,
    val behandlingstema: String? = null,
    val tittel: String? = null,
    val bruker: Bruker? = null,
    val journalforendeEnhet: String? = null,
    val kanal: String? = null,
    val eksternReferanseId: String? = null,
)
