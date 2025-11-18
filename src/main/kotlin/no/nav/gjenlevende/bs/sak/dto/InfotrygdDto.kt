package no.nav.gjenlevende.bs.sak.dto

import java.time.LocalDate

data class StønadTypeStatistikk(
    val kodeRutine: String,
    val antall: Int,
)

data class StønadTypeStatistikkResponse(
    val stønadtyper: List<StønadTypeStatistikk>,
    val totaltAntall: Int,
)

enum class StønadType(
    val kodeRutine: String,
) {
    BARNETILSYN("GB"),
    SKOLEPENGER("GU"),
}

data class PeriodeResponse(
    val stønadType: StønadType,
    val fom: LocalDate,
    val tom: LocalDate?,
    val beløp: Int? = null,
    val vedtakId: Long,
    val stønadId: Long,
    val barn: List<BarnInfo> = emptyList(),
)

data class BarnInfo(
    val personLopenr: Long,
    val fom: LocalDate,
    val tom: LocalDate?,
)

data class PersonPerioderResponse(
    val personIdent: String,
    val barnetilsyn: List<PeriodeResponse> = emptyList(),
    val skolepenger: List<PeriodeResponse> = emptyList(),
) {
    val harPerioder: Boolean
        get() = barnetilsyn.isNotEmpty() || skolepenger.isNotEmpty()
}
