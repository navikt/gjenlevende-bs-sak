package no.nav.gjenlevende.bs.sak.felles.sikkerhet

import no.nav.gjenlevende.bs.sak.tilgangskontroll.Avvisningsgrunn

class ManglerTilgang(
    val melding: String,
    val frontendFeilmelding: String,
    val avvisningsgrunn: Avvisningsgrunn? = null,
) : RuntimeException(melding)
