package no.nav.gjenlevende.bs.sak.dto

import java.time.LocalDate

data class PersonPerioderRequest(
    val personident: String,
)

data class PeriodeResponse(
    val fom: LocalDate,
    val tom: LocalDate?,
    val vedtakId: Long,
    val stønadId: Long,
    val barn: List<BarnInfo> = emptyList(),
)

data class BarnInfo(
    val personLøpenummer: Long,
    val fom: LocalDate,
    val tom: LocalDate?,
)

data class PersonPerioderResponse(
    val personident: String,
    val barnetilsyn: List<PeriodeResponse> = emptyList(),
    val skolepenger: List<PeriodeResponse> = emptyList(),
) {
    val harPerioder: Boolean
        get() = barnetilsyn.isNotEmpty() || skolepenger.isNotEmpty()
}
