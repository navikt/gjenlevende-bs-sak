package no.nav.gjenlevende.bs.sak.tilgangskontroll

import com.fasterxml.jackson.annotation.JsonProperty

enum class RegelType {
    @JsonProperty("KOMPLETT_REGELTYPE")
    KOMPLETT_REGELTYPE,

    @JsonProperty("KJERNE_REGELTYPE")
    KJERNE_REGELTYPE,
}

data class BulkTilgangsRequest(
    val brukerIdenter: List<String>,
)

data class BulkTilgangsResponse(
    val ansattId: String,
    val resultater: List<TilgangsResultat>,
)

data class TilgangsResultat(
    val brukerId: String,
    val status: Int,
    val detaljer: AvvisningsDetaljer? = null,
)

data class AvvisningsDetaljer(
    val type: String,
    val title: String,
    val status: Int,
    val instance: String,
    val brukerIdent: String,
    val navIdent: String,
    val begrunnelse: String,
    val traceId: String,
    val kanOverstyres: Boolean,
)

data class EnkelTilgangsResponse(
    val ansattId: String,
    val personident: String,
    val harTilgang: Boolean,
    val avvisningsgrunn: String? = null,
    val begrunnelse: String? = null,
)
