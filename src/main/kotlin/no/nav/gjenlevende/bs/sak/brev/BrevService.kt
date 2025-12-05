package no.nav.gjenlevende.bs.sak.brev

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Service

@Service
class BrevService(
    private val brevClient: BrevClient,
    private val familieDokumentClient: FamilieDokumentClient,
) {
    fun lagVedtaksbrev(
        saksbehandling: Saksbehandling,
        brevrequest: JsonNode,
        brevmal: String,
    ): ByteArray {
        val html =
            brevClient.genererHtml(
                brevmal = brevmal,
                saksbehandlerBrevrequest = brevrequest,
                saksbehandlersignatur = "signatur", // TODO
                saksbehandlerEnhet = "enhet", // TODO
                skjulBeslutterSignatur = false, // TODO
            )

        return familieDokumentClient.genererPdfFraHtml(html)
    }

    companion object {
        const val BESLUTTER_SIGNATUR_PLACEHOLDER = "BESLUTTER_SIGNATUR"
        const val BESLUTTER_ENHET_PLACEHOLDER = "BESLUTTER_ENHET"
        const val BESLUTTER_VEDTAKSDATO_PLACEHOLDER = "BESLUTTER_VEDTAKSDATO"
    }
}

data class Saksbehandling(
    val ident: String,
    val saksbehandler: String,
) // TODO
