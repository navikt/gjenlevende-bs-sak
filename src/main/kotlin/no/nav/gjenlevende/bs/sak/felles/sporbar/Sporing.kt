package no.nav.gjenlevende.bs.sak.felles.sporbar

import no.nav.gjenlevende.bs.sak.felles.sikkerhet.SikkerhetContext
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class Sporing(
    val opprettetAv: String = SikkerhetContext.hentSaksbehandlerEllerSystembruker(),
    val opprettetTid: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
    val endretAv: String = SikkerhetContext.hentSaksbehandlerEllerSystembruker(),
    val endretTid: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
)
