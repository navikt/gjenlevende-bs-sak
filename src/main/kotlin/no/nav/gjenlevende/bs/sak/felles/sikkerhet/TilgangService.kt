package no.nav.gjenlevende.bs.sak.felles.sikkerhet

import no.nav.gjenlevende.bs.sak.felles.auditlogger.AuditLogger
import no.nav.gjenlevende.bs.sak.felles.auditlogger.AuditLoggerEvent
import no.nav.gjenlevende.bs.sak.felles.auditlogger.Sporingsdata
import no.nav.gjenlevende.bs.sak.tilgangskontroll.TilgangsmaskinClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TilgangService(
    private val auditLogger: AuditLogger,
    private val tilgangsmaskinClient: TilgangsmaskinClient,
) {
    private val logger = LoggerFactory.getLogger(TilgangService::class.java)

    fun validerTilgangTilPersonMedBarn(
        personIdent: String,
        event: AuditLoggerEvent,
    ) {
        val resultat = tilgangsmaskinClient.sjekkTilgang(personIdent)
        val tilgang = resultat.tilTilgang()

        auditLogger.log(Sporingsdata(event, personIdent, tilgang))

        if (!resultat.harTilgang) {
            logger.warn(
                "Saksbehandler ${SikkerhetContext.hentSaksbehandlerEllerSystembruker()} " +
                    "har ikke tilgang til person: ${resultat.avvisningsgrunn}",
            )
            throw ManglerTilgang(
                melding =
                    "Saksbehandler ${SikkerhetContext.hentSaksbehandlerEllerSystembruker()} " +
                        "har ikke tilgang til person",
                frontendFeilmelding = "Mangler tilgang til opplysningene. ${tilgang.utledÅrsakstekst()}",
                avvisningsgrunn = resultat.avvisningsgrunn,
            )
        }
    }
}
