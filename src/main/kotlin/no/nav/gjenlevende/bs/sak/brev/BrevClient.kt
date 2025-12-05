package no.nav.gjenlevende.bs.sak.brev

import com.fasterxml.jackson.databind.JsonNode
import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.gjenlevende.bs.sak.brev.BrevService.Companion.BESLUTTER_ENHET_PLACEHOLDER
import no.nav.gjenlevende.bs.sak.brev.BrevService.Companion.BESLUTTER_SIGNATUR_PLACEHOLDER
import no.nav.gjenlevende.bs.sak.brev.BrevService.Companion.BESLUTTER_VEDTAKSDATO_PLACEHOLDER
import no.nav.gjenlevende.bs.sak.util.medContentTypeJsonUTF8
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class BrevClient(
    @Value("\${FAMILIE_BREV_API_URL}")
    private val brevUri: String,
    @Qualifier("utenAuth") // TODO
    private val restOperations: RestOperations,
) : AbstractPingableRestClient(restOperations, "familie.brev") {
    override val pingUri: URI = URI.create("$brevUri/api/status")

    fun genererHtml(
        brevmal: String,
        saksbehandlerBrevrequest: JsonNode,
        saksbehandlersignatur: String,
        saksbehandlerEnhet: String?,
        skjulBeslutterSignatur: Boolean,
    ): String {
        val url = URI.create("$brevUri/api/ef-brev/avansert-dokument/bokmaal/$brevmal/html")

        return postForEntity(
            url,
            BrevRequestMedSignaturer(
                brevFraSaksbehandler = saksbehandlerBrevrequest,
                saksbehandlersignatur = saksbehandlersignatur,
                saksbehandlerEnhet = saksbehandlerEnhet,
                besluttersignatur = BESLUTTER_SIGNATUR_PLACEHOLDER,
                beslutterEnhet = BESLUTTER_ENHET_PLACEHOLDER,
                skjulBeslutterSignatur = skjulBeslutterSignatur,
                datoPlaceholder = BESLUTTER_VEDTAKSDATO_PLACEHOLDER,
            ),
            HttpHeaders().medContentTypeJsonUTF8(),
        )
    }
}

data class BrevRequestMedSignaturer(
    val brevFraSaksbehandler: JsonNode,
    val saksbehandlersignatur: String,
    val saksbehandlerEnhet: String?,
    val besluttersignatur: String?,
    val beslutterEnhet: String?,
    val skjulBeslutterSignatur: Boolean,
    val datoPlaceholder: String,
)
