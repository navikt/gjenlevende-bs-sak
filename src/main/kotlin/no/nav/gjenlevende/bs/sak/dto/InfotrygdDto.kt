package no.nav.gjenlevende.bs.sak.dto

data class StønadTypeStatistikk(
    val kodeRutine: String,
    val antall: Int,
)

data class StønadTypeStatistikkResponse(
    val stønadtyper: List<StønadTypeStatistikk>,
    val totaltAntall: Int,
)
