package no.nav.gjenlevende.bs.sak.tilgangskontroll

import no.nav.gjenlevende.bs.sak.felles.auditlogger.Tilgang

data class TilgangResultat(
    val harTilgang: Boolean,
    val avvisningsgrunn: Avvisningsgrunn? = null,
    val begrunnelse: String? = null,
) {
    companion object {
        fun godkjent() = TilgangResultat(harTilgang = true)

        fun avvist(
            avvisningsgrunn: Avvisningsgrunn,
            begrunnelse: String? = null,
        ) = TilgangResultat(
            harTilgang = false,
            avvisningsgrunn = avvisningsgrunn,
            begrunnelse = begrunnelse ?: avvisningsgrunn.beskrivelse,
        )

        fun avvist(begrunnelse: String) =
            TilgangResultat(
                harTilgang = false,
                avvisningsgrunn = Avvisningsgrunn.UKJENT,
                begrunnelse = begrunnelse,
            )
    }

    fun tilTilgang(): Tilgang = Tilgang(harTilgang, begrunnelse)
}
