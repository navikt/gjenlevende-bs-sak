package no.nav.gjenlevende.bs.sak.felles.sikkerhet

import no.nav.gjenlevende.bs.sak.felles.auditlogger.AuditLogger
import no.nav.gjenlevende.bs.sak.infrastruktur.exception.ManglerTilgang
import no.nav.gjenlevende.bs.sak.opplysninger.FamilieIntegrasjonerClient
import no.nav.gjenlevende.bs.sak.tilgangskontroll.TilgangsmaskinClient
import org.springframework.stereotype.Service

@Service
class TilgangService(
    private val auditLogger: AuditLogger,
    private val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
    private val tilgangsmaskinClient: TilgangsmaskinClient,
) {
    fun validerTilgangTilPersonMedBarn(
        personIdent: String,
    ) {
        val tilgang = familieIntegrasjonerClient.sjekkTilgangTilPersonMedRelasjoner(personIdent)
        if (!tilgang.harTilgang) {
            throw ManglerTilgang(
                melding =
                    "Saksbehandler ${SikkerhetContext.hentSaksbehandlerEllerSystembruker()} " +
                        "har ikke tilgang til person eller dets barn",
                frontendFeilmelding = "Mangler tilgang til opplysningene. ${tilgang.utledÅrsakstekst()}",
            )
        }
    }

    fun validerTilgangForPersonMotTilgangsmaskin(personident: String): Boolean {
        val navIdent = SikkerhetContext.hentSaksbehandler()

        val tilgangReponse = tilgangsmaskinClient.sjekkTilgangEnkel(navIdent = navIdent, personident = personident)

        return tilgangReponse.harTilgang
    }
}
