package no.nav.gjenlevende.bs.sak.pdl

import no.nav.gjenlevende.bs.sak.felles.OAuth2RestOperationsFactory
import no.nav.gjenlevende.bs.sak.texas.TexasClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Configuration
class PdlWebClientConfig {
    @Bean
    fun pdlWebClient(
        @Value("\${PDL_URL}")
        pdlUrl: String,
    ): WebClient =
        WebClient
            .builder()
            .baseUrl(pdlUrl)
            .defaultHeader("Content-Type", "application/json")
            .build()
}

@Component
class PdlClient(
    private val pdlWebClient: WebClient,
    private val texasClient: TexasClient,
    @Value("\${PDL_SCOPE}")
    private val pdlScope: String
) {
    private val logger = LoggerFactory.getLogger(PdlClient::class.java)

//    val pdlPath: URI =
//        UriComponentsBuilder
//            .fromUri(pdlUrl)
//            .pathSegment("graphql")
//            .build()
//            .toUri()

    val obo = texasClient.hentOboToken(pdlScope)


    fun <T> utførQuery(
        query: String,
        variables: Map<String, String>,
        responstype: ParameterizedTypeReference<PdlResponse<T>>,
        operasjon: String,
    ): T? {
        val request =
            PdlRequest(
                query = query,
                variables = variables,
            )

        val headers = lagPdlHeaders()
        val entity = HttpEntity(request, headers)

        logger.info("Utfører PDL-operasjon: $operasjon")

        return try {
            val response =
                restTemplate.exchange(
                    pdlPath,
                    HttpMethod.POST,
                    entity,
                    responstype,
                )

            val pdlResponse =
                response.body
                    ?: throw PdlException("Ingen respons fra PDL for $operasjon")

            håndterPdlErrors(pdlResponse.errors, operasjon)

            pdlResponse.data
        } catch (e: Exception) {
            when (e) {
                is PdlException -> {
                    throw e
                }

                else -> {
                    logger.error("Teknisk feil ved PDL-operasjon: $operasjon", e)
                    throw PdlException("Teknisk feil ved $operasjon", e)
                }
            }
        }
    }

    private fun lagPdlHeaders(): HttpHeaders =
        HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set("Tema", "EYO")
            set("behandlingsnummer", "B373")
        }

    private fun håndterPdlErrors(
        errors: List<PdlError>?,
        operasjon: String,
    ) {
        if (errors != null && errors.isNotEmpty()) {
            logger.error("Feil fra PDL ved $operasjon: $errors")
            val firstError = errors.firstOrNull()
            throw PdlException(
                "Feil ved $operasjon: ${firstError?.message ?: "Ukjent feil"}",
            )
        }
    }
}

class PdlException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
