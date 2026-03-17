package no.nav.gjenlevende.bs.sak.iverksett

import no.nav.gjenlevende.bs.sak.iverksett.domene.ArkiverDokumentResponse
import no.nav.gjenlevende.bs.sak.iverksett.domene.DistribuerJournalpostRequest
import no.nav.gjenlevende.bs.sak.iverksett.domene.DistribuerJournalpostResponse
import no.nav.gjenlevende.bs.sak.iverksett.domene.JournalpostRequest
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

    fun arkiverDokument(
        journalpostRequest: JournalpostRequest,
        forsoekFerdigstill: Boolean,
    ): ArkiverDokumentResponse {
        val headers =
            HttpHeaders().apply {
                setBearerAuth(texasClient.hentMaskinToken(targetAudience = dokarkivScope.toString()))
                this.contentType = MediaType.APPLICATION_JSON
                this.accept = listOf(MediaType.APPLICATION_JSON)
            }

        return webClient
            .post()
            .uri { it.path(OPPRETT_JOURNALPOST).queryParam("forsoekFerdigstill", forsoekFerdigstill).build() }
            .headers { it.addAll(headers) }
            .bodyValue(journalpostRequest)
            .retrieve()
            .bodyToMono<ArkiverDokumentResponse>()
            .block() ?: error("Ingen response ved arkivering av dokument")
    }

    fun distribuerDokument(distribuerJournalpostRequest: DistribuerJournalpostRequest) {
        val headers =
            HttpHeaders().apply {
                setBearerAuth(texasClient.hentMaskinToken(targetAudience = dokarkivScope.toString()))
                this.contentType = MediaType.APPLICATION_JSON
                this.accept = listOf(MediaType.APPLICATION_JSON)
            }
        webClient
            .post()
            .uri { it.path(DISTRIBUER_DOKUMENT).build(distribuerJournalpostRequest) }
            .headers { it.addAll(headers) }
            .retrieve()
            .bodyToMono<DistribuerJournalpostResponse>()
            .block()
    }

    companion object {
        const val OPPRETT_JOURNALPOST = "rest/journalpostapi/v1/journalpost"
        const val DISTRIBUER_DOKUMENT = "rest/v1/distribuerjournalpost"
    }
}
