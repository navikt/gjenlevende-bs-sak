package no.nav.gjenlevende.bs.sak.tilgangskontroll

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.util.UUID

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

data class TilgangsResponse(
    val ansattId: String,
    val bruker: BrukerInfo? = null,
    val grupper: List<Gruppe> = emptyList(),
)

data class BrukerInfo(
    val brukerIds: BrukerIds,
    val geografiskTilknytning: GeografiskTilknytning? = null,
    val påkrevdeGrupper: List<PåkrevdGruppe> = emptyList(),
    val familie: Familie? = null,
    val dødsdato: LocalDate? = null,
    val oppslagId: String,
    val harUkjentBosted: Boolean = false,
    val harUtenlandskBosted: Boolean = false,
)

data class BrukerIds(
    val aktivBrukerId: String,
    val oppslagId: String,
    val historiskeIds: List<String> = emptyList(),
    val aktørId: String? = null,
)

data class GeografiskTilknytning(
    val kommune: Verdi? = null,
    val bydel: Verdi? = null,
)

// TODO: Er bare satt som verdi, skal fjernes.
data class Verdi(
    val verdi: String,
)

enum class PåkrevdGruppe {
    STRENGT_FORTROLIG,
    STRENGT_FORTROLIG_UTLAND,
    FORTROLIG,
    EGEN_ANSATT,
}

data class Familie(
    val foreldre: List<Relasjon> = emptyList(),
    val barn: List<Relasjon> = emptyList(),
    val søsken: List<Relasjon> = emptyList(),
    val partnere: List<Relasjon> = emptyList(),
)

data class Relasjon(
    val brukerId: String,
    val relasjon: String,
)

data class Gruppe(
    val id: UUID,
    val displayName: String,
)

data class EnkelTilgangsResponse(
    val ansattId: String,
    val personident: String,
    val harTilgang: Boolean,
    val avvisningsgrunn: String? = null,
    val begrunnelse: String? = null,
)

// TODO: Skal fikse senere.
enum class Avvisningsgrunn {
    AVVIST_STRENGT_FORTROLIG_ADRESSE,
    AVVIST_STRENGT_FORTROLIG_UTLAND,
    AVVIST_AVDØD,
    AVVIST_PERSON_UTLAND,
    AVVIST_SKJERMING,
    AVVIST_FORTROLIG_ADRESSE,
    AVVIST_UKJENT_BOSTED,
    AVVIST_GEOGRAFISK,
    AVVIST_HABILITET,
}
