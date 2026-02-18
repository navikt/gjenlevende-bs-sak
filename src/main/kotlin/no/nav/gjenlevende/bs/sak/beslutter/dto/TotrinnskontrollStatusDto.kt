package no.nav.gjenlevende.bs.sak.beslutter.dto

import java.time.LocalDateTime

data class TotrinnskontrollStatusDto(
    val status: TotrinnskontrollStatus,
    val totrinnskontroll: TotrinnskontrollDto? = null,
)

data class TotrinnskontrollDto(
    val opprettetAv: String,
    val opprettetTid: LocalDateTime,
    val godkjent: Boolean? = null,
    val begrunnelse: String? = null,
    val årsakerUnderkjent: List<ÅrsakUnderkjent> = emptyList(),
)

enum class TotrinnskontrollStatus {
    TOTRINNSKONTROLL_UNDERKJENT,
    KAN_FATTE_VEDTAK,
    IKKE_AUTORISERT,
    UAKTUELT,
}
