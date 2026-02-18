package no.nav.gjenlevende.bs.sak.beslutter.dto

data class BeslutteVedtakDto(
    val godkjent: Boolean,
    val begrunnelse: String? = null,
    val årsakerUnderkjent: List<ÅrsakUnderkjent> = emptyList(),
)

enum class ÅrsakUnderkjent {
    ÅRSAK_OG_KRAVDATO,
    VILKÅR,
    VEDTAK_OG_BEREGNING,
    VEDTAKSBREV,
    RETUR_ETTER_ØNSKE_FRA_SAKSBEHANDLER,
}
