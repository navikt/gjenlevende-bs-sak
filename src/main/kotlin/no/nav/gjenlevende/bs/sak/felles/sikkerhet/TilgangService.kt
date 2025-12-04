package no.nav.gjenlevende.bs.sak.felles.sikkerhet

import no.nav.gjenlevende.bs.sak.felles.auditlogger.AuditLogger
import no.nav.gjenlevende.bs.sak.felles.auditlogger.AuditLoggerEvent
import org.springframework.stereotype.Service

@Service
class TilgangService(
    private val auditLogger: AuditLogger,
) {
    fun validerTilgangTilPersonMedBarn(
        personIdent: String,
        event: AuditLoggerEvent,
    ) {
        /*
        val tilgang = harTilgangTilPersonMedRelasjoner(personIdent)
        auditLogger.log(Sporingsdata(event, personIdent, tilgang))
        if (!tilgang.harTilgang) {
            secureLogger.warn(
                "Saksbehandler ${SikkerhetContext.hentSaksbehandlerEllerSystembruker()} " +
                    "har ikke tilgang til $personIdent eller dets barn",
            )
            throw ManglerTilgang(
                melding =
                    "Saksbehandler ${SikkerhetContext.hentSaksbehandlerEllerSystembruker()} " +
                        "har ikke tilgang til person eller dets barn",
                frontendFeilmelding = "Mangler tilgang til opplysningene. ${tilgang.utledÅrsakstekst()}",
            )
        }

         */
    }
}
