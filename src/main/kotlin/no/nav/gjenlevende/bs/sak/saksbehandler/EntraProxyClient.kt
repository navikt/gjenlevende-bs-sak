package no.nav.gjenlevende.bs.sak.saksbehandler

import SaksbehandlerResponse
import java.time.Duration
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.SikkerhetContext
import no.nav.gjenlevende.bs.sak.texas.TexasClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class EntraProxyClient(
    @Value("\${ENTRA_PROXY_URL}") private val entraProxyUrl: String,
    @Value("\${ENTRA_PROXY_SCOPE}") private val entraProxyAudience: String,
    private val texasClient: TexasClient,
) {
    private val logger = LoggerFactory.getLogger(EntraProxyClient::class.java)
    private val webClient =
        WebClient
            .builder()
            .baseUrl(entraProxyUrl)
            .build()

    companion object {
        private const val TIMEOUT_SEKUNDER = 10L
    }

    fun hentSaksbehandlerInfo(navIdent: String): SaksbehandlerResponse {
//        val oboToken =
//            texasClient.hentOboToken(
//                targetAudience = entraProxyAudience,
//            )
        val token = SikkerhetContext.hentBrukerToken()
//        logger.info("OBO token: $oboToken")
        return webClient
            .get()
            .uri("/api/v1/ansatt/$navIdent")
            .header("Authorization", "Bearer $token")
            .retrieve()
            .bodyToMono<SaksbehandlerResponse>()
            .timeout(Duration.ofSeconds(TIMEOUT_SEKUNDER))
            .doOnNext { response ->
                logger.info("Hentet saksbehandlerinfo for navIdent: ${response.navIdent}")
            }.doOnError { logger.error("Feilet å hente saksbehandlerinfo: $it") }
            .block() ?: throw RuntimeException("Klarte ikke å hente saksbehandlerinfo fra entra-proxy")
    }
}
