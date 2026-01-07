package no.nav.gjenlevende.bs.sak.brev

import no.nav.gjenlevende.bs.sak.felles.OAuth2RestOperationsFactory
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
class FamilieDokumentClient(
    @Value("\${FAMILIE_DOKUMENT_URL}") private val familieDokumentUrl: URI,
    @Value("\${familie-integrasjoner.oauth.registration-id}") registrationId: String,
    oauth2RestFactory: OAuth2RestOperationsFactory,
) {
    private val restTemplate: RestOperations = oauth2RestFactory.create(registrationId)

    fun genererPdfFraHtml(html: String): ByteArray {
        val htmlTilPdfURI: URI =
            UriComponentsBuilder
                .fromUri(familieDokumentUrl)
                .pathSegment(HTML_TIL_PDF)
                .build()
                .toUri()
        val headers =
            HttpHeaders().apply {
                this.contentType = MediaType.TEXT_HTML
                this.accept = listOf(MediaType.APPLICATION_PDF)
            }
        val entity =
            HttpEntity(
                html.encodeToByteArray(),
                headers,
            )

        return restTemplate
            .exchange<ByteArray>(
                htmlTilPdfURI,
                HttpMethod.POST,
                entity,
            ).body ?: error("Ingen response ved henting av tilgang til person med relasjoner")
    }

    companion object {
        const val HTML_TIL_PDF = "api/html-til-pdf"
    }
}
