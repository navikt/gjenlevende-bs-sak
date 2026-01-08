package no.nav.gjenlevende.bs.sak.felles.sikkerhet

import no.nav.gjenlevende.bs.sak.infrastruktur.exception.ManglerTilgang
import no.nav.gjenlevende.bs.sak.pdl.PdlService
import no.nav.gjenlevende.bs.sak.tilgangskontroll.Avvisningskode
import no.nav.gjenlevende.bs.sak.tilgangskontroll.TilgangsResultat
import no.nav.gjenlevende.bs.sak.tilgangskontroll.TilgangsmaskinClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TilgangService(
    private val tilgangsmaskinClient: TilgangsmaskinClient,
    private val pdlService: PdlService,
) {
    private val logger = LoggerFactory.getLogger(TilgangService::class.java)

    fun validerTilgangTilPersonMedRelasjoner(personident: String) {
        val brukerToken = SikkerhetContext.hentBrukerToken()
        val barnPersonidenter = pdlService.hentBarnPersonidenter(personident)
        val allePersonidenter = listOf(personident) + barnPersonidenter

        logger.info("Validerer tilgang til ${allePersonidenter.size} person(er) via tilgangsmaskin")

        val respons =
            tilgangsmaskinClient.sjekkTilgangBulk(
                brukerToken = brukerToken,
                personidenter = allePersonidenter,
            )

        val avvistePersoner = respons.resultater.filter { it.status != 204 }

        if (avvistePersoner.isNotEmpty()) {
            val avvisningsdetaljer = avvistePersoner.map { it.tilAvvisningsdetaljer() }
            logger.warn(
                "Tilgang avvist for saksbehandler ${respons.navIdent}. " +
                    "Avviste: $avvisningsdetaljer",
            )

            val forsteBegrunnelse = avvisningsdetaljer.firstOrNull()?.begrunnelse ?: "Ukjent årsak"

            throw ManglerTilgang(
                melding = "Saksbehandler ${respons.navIdent} har ikke tilgang til person eller dets barn",
                frontendFeilmelding = "Mangler tilgang til opplysningene. Årsak: $forsteBegrunnelse",
            )
        }

        logger.info("Tilgang OK for saksbehandler ${respons.navIdent} til ${allePersonidenter.size} person(er)")
    }

    private fun TilgangsResultat.tilAvvisningsdetaljer(): Avvisningsdetaljer {
        val detaljer = detaljer as? Map<*, *>
        return Avvisningsdetaljer(
            personident = personident,
            avvisningskode =
                detaljer
                    ?.get("title")
                    ?.toString()
                    ?.let { runCatching { Avvisningskode.valueOf(it) }.getOrNull() },
            begrunnelse = detaljer?.get("begrunnelse")?.toString(),
        )
    }

    private data class Avvisningsdetaljer(
        val personident: String,
        val avvisningskode: Avvisningskode?,
        val begrunnelse: String?,
    )
}
