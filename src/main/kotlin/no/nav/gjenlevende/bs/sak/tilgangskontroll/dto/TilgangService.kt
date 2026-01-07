package no.nav.gjenlevende.bs.sak.tilgangskontroll.dto

import no.nav.gjenlevende.bs.sak.felles.sikkerhet.SikkerhetContext
import no.nav.gjenlevende.bs.sak.tilgangskontroll.TilgangsmaskinClient
import org.springframework.stereotype.Component

@Component
class TilgangService(
    private val tilgangsmaskinClient: TilgangsmaskinClient,
) {
    fun validerTilgangForPerson(personident: String): Boolean {
        val navIdent = SikkerhetContext.hentSaksbehandler()

        val tilgangReponse = tilgangsmaskinClient.sjekkTilgangEnkel(navIdent = navIdent, personident = personident)

        return tilgangReponse.harTilgang
    }
}
