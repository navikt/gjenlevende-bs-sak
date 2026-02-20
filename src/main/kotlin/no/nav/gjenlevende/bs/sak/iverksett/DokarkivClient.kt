package no.nav.gjenlevende.bs.sak.iverksett

import no.nav.gjenlevende.bs.sak.iverksett.domene.ArkiverDokumentRequest
import no.nav.gjenlevende.bs.sak.texas.TexasClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.net.URI

@Component
class DokarkivClient(
    @Value("\${DOKARKIV_URL}") private val dokarkivUrl: URI,
    @Value("\${DOKARKIV_SCOPE}") private val dokarkivScope: URI,
    private val texasClient: TexasClient,
) {
    val webClient =
        WebClient
            .builder()
            .baseUrl(dokarkivUrl.toString())
            .defaultHeader("Content-Type", "application/json")
            .build()

    fun arkiverDokument(arkiverDokumentRequest: ArkiverDokumentRequest): String {
        val headers =
            HttpHeaders().apply {
                setBearerAuth(texasClient.hentMaskinToken(targetAudience = dokarkivScope.toString()))
                this.contentType = MediaType.APPLICATION_JSON
                this.accept = listOf(MediaType.TEXT_PLAIN)
            }

        return webClient
            .post()
            .uri(OPPRETT_JOURNALPOST)
            .headers { it.addAll(headers) }
            .bodyValue(arkiverDokumentRequest) // TODO response av noe slag her? journalpostid elns
            .retrieve()
            .bodyToMono<String>()
            .block() ?: error("Ingen response ved henting av tilgang til person med relasjoner")
    }

    companion object {
        const val OPPRETT_JOURNALPOST = "rest/journalpostapi/v1/journalpost"
    }
}
