package no.nav.gjenlevende.bs.sak.felles.sikkerhet

import no.nav.gjenlevende.bs.sak.felles.auditlogger.AuditLogger
import no.nav.gjenlevende.bs.sak.infrastruktur.exception.ManglerTilgang
import no.nav.gjenlevende.bs.sak.opplysninger.FamilieIntegrasjonerClient
import org.springframework.stereotype.Service

@Service
class TilgangService(
    private val auditLogger: AuditLogger,
    private val familieIntegrasjonerClient: FamilieIntegrasjonerClient,
) {
    fun validerTilgangTilPersonMedBarn(
        personident: String,
    ) {
        val tilgang = familieIntegrasjonerClient.sjekkTilgangTilPersonMedRelasjoner(personident)
        if (!tilgang.harTilgang) {
            throw ManglerTilgang(
                melding =
                    "Saksbehandler ${SikkerhetContext.hentSaksbehandlerEllerSystembruker()} " +
                        "har ikke tilgang til person eller dets barn",
                frontendFeilmelding = "Mangler tilgang til opplysningene. ${tilgang.utledÅrsakstekst()}",
            )
        }
    }
}
