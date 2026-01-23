package no.nav.gjenlevende.bs.sak.saksbehandler

import no.nav.gjenlevende.bs.sak.client.AzureGraphClient
import no.nav.gjenlevende.bs.sak.client.domain.Saksbehandler
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SaksbehandlerService(
    private val azureGraphRestClient: AzureGraphClient,
) {
    private val lengdeNavIdent = 7

    fun hentSaksbehandler(id: String): Saksbehandler {
        if (id == ID_VEDTAKSLØSNINGEN) {
            return Saksbehandler(
                azureId = UUID.randomUUID(),
                navIdent = ID_VEDTAKSLØSNINGEN,
                fornavn = "Vedtaksløsning",
                etternavn = "Nav",
                enhet = "9999",
            )
        }
        val azureAdBruker =
            if (id.length == lengdeNavIdent) {
                val azureAdBrukere = azureGraphRestClient.finnSaksbehandler(id)

                if (azureAdBrukere.value.size != 1) {
                    error("Feil ved søk. Oppslag på navIdent $id returnerte ${azureAdBrukere.value.size} forekomster.")
                }
                azureAdBrukere.value.first()
            } else {
                azureGraphRestClient.hentSaksbehandler(id)
            }

        return Saksbehandler(
            azureId = azureAdBruker.id,
            navIdent = azureAdBruker.onPremisesSamAccountName,
            fornavn = azureAdBruker.givenName,
            etternavn = azureAdBruker.surname,
            enhet = azureAdBruker.streetAddress,
            enhetsnavn = azureAdBruker.city,
        )
    }

    fun hentNavIdent(saksbehandlerId: String): String = saksbehandlerId.takeIf { it.length == lengdeNavIdent } ?: hentSaksbehandler(saksbehandlerId).navIdent

    companion object {
        const val ID_VEDTAKSLØSNINGEN = "VL"
    }
}
