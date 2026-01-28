package no.nav.gjenlevende.bs.sak.pdl

import no.nav.gjenlevende.bs.sak.texas.TexasClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

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
    private val pdlScope: String,
) {
    private val logger = LoggerFactory.getLogger(PdlClient::class.java)

    fun <T> utførQuery(
        query: String,
        variables: Map<String, String>,
        operasjon: String,
    ): T? {
        val obo = texasClient.hentOboToken(pdlScope)
        val request =
            PdlRequest(
                query = query,
                variables = variables,
            )

        logger.info("Utfører PDL-operasjon: $operasjon")

        val pdlResponse =
            pdlWebClient
                .post()
                .header("Authorization", "Bearer $obo")
                .headers { it.addAll(lagPdlHeaders()) }
                .bodyValue(request)
                .retrieve()
                .bodyToMono<PdlResponse<T>>()
                .switchIfEmpty(Mono.error(NoSuchElementException("Tom respons fra PDL")))
                .block() ?: throw RuntimeException("Klarte ikke å hente data fra PDL for operasjon: $operasjon")

        håndterPdlErrors(pdlResponse.errors, operasjon)

        return pdlResponse.data
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
