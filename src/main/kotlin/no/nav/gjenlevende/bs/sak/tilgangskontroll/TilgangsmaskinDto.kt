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

data class AnsattInfoResponse(
    val ansattId: String,
    val bruker: BrukerInfo? = null,
    val grupper: List<GruppeInfo> = emptyList(),
)

data class GruppeInfo(
    val id: String,
    val displayName: String = "N/A",
)

data class BrukerInfo(
    val brukerIds: BrukerIds,
    val geografiskTilknytning: GeografiskTilknytning? = null,
    val familie: FamilieInfo? = null,
    val harUkjentBosted: Boolean = false,
    val harUtenlandskBosted: Boolean = false,
    val oppslagId: String,
)

data class BrukerIds(
    val aktivBrukerId: String,
    val oppslagId: String,
    val historiskeIds: List<String> = emptyList(),
)

data class GeografiskTilknytning(
    val kommune: KommuneInfo? = null,
)

data class KommuneInfo(
    val verdi: String,
)

data class FamilieInfo(
    val foreldre: List<FamilieRelasjon> = emptyList(),
    val barn: List<FamilieRelasjon> = emptyList(),
    val partnere: List<FamilieRelasjon> = emptyList(),
)

data class FamilieRelasjon(
    val brukerId: String,
    val relasjon: String,
)
