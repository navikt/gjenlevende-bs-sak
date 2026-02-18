package no.nav.gjenlevende.bs.sak.vedtak

import no.nav.gjenlevende.bs.sak.felles.sikkerhet.SikkerhetContext
import no.nav.gjenlevende.bs.sak.felles.sporbar.SporbarUtils
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.MappedCollection
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID

data class Vedtak(
    @Id
    val behandlingId: UUID,
    val resultatType: ResultatType,
    val begrunnelse: String? = null,
    @MappedCollection(idColumn = "behandling_id", "behandling_id")
    val barnetilsynperioder: List<Barnetilsynperiode>,
    val saksbehandlerIdent: String,
    @Column("opphor_fom")
    val opphørFom: YearMonth? = null,
    val beslutterIdent: String? = null,
    val opprettetTid: LocalDateTime = SporbarUtils.now(),
    val opprettetAv: String = SikkerhetContext.hentSaksbehandlerEllerSystembruker(),
)

data class Barnetilsynperiode(
    @Id
    val behandlingId: UUID,
    val datoFra: YearMonth,
    val datoTil: YearMonth,
    val utgifter: BigDecimal,
    val barn: List<UUID>,
    val periodetype: PeriodetypeBarnetilsyn,
    val aktivitetstype: AktivitetstypeBarnetilsyn,
)

data class BarnetilsynBeregningRequest(
    val barnetilsynBeregning: List<BarnetilsynBeregning>,
)

data class BarnetilsynBeregning(
    val datoFra: YearMonth,
    val datoTil: YearMonth,
    val utgifter: BigDecimal,
    val barn: List<UUID>,
    val periodetype: PeriodetypeBarnetilsyn,
)

data class BeløpsperioderDto(
    val datoFra: YearMonth,
    val datoTil: YearMonth,
    val utgifter: BigDecimal,
    val antallBarn: Int,
    val beløp: Int,
    val periodetype: PeriodetypeBarnetilsyn,
)

enum class ResultatType {
    INNVILGET,
    AVSLÅTT,
    HENLAGT,
    OPPHØR,
}

fun ResultatType.tilBehandlingResultat(): no.nav.gjenlevende.bs.sak.behandling.BehandlingResultat =
    when (this) {
        ResultatType.INNVILGET -> no.nav.gjenlevende.bs.sak.behandling.BehandlingResultat.INNVILGET
        ResultatType.AVSLÅTT -> no.nav.gjenlevende.bs.sak.behandling.BehandlingResultat.AVSLÅTT
        ResultatType.HENLAGT -> no.nav.gjenlevende.bs.sak.behandling.BehandlingResultat.HENLAGT
        ResultatType.OPPHØR -> no.nav.gjenlevende.bs.sak.behandling.BehandlingResultat.OPPHØRT
    }

enum class PeriodetypeBarnetilsyn {
    ORDINÆR,
    INGEN_STØNAD,
}

enum class AktivitetstypeBarnetilsyn {
    I_ARBEID,
    FORBIGÅENDE_SYKDOM,
}

data class VedtakDto(
    val resultatType: ResultatType,
    val begrunnelse: String? = null,
    val barnetilsynperioder: List<Barnetilsynperiode>,
    val saksbehandlerIdent: String? = null,
    val opphørFom: YearMonth? = null,
    val beslutterIdent: String? = null,
)

fun VedtakDto.tilVedtak(behandlingId: UUID): Vedtak =
    Vedtak(
        behandlingId = behandlingId,
        resultatType = this.resultatType,
        begrunnelse = this.begrunnelse,
        barnetilsynperioder = this.barnetilsynperioder,
        saksbehandlerIdent = SikkerhetContext.hentSaksbehandlerEllerSystembruker(),
        opphørFom = this.opphørFom,
        beslutterIdent = this.beslutterIdent,
    )

fun Vedtak.tilDto(): VedtakDto =
    VedtakDto(
        resultatType = this.resultatType,
        begrunnelse = this.begrunnelse,
        barnetilsynperioder = this.barnetilsynperioder,
        saksbehandlerIdent = this.saksbehandlerIdent,
        opphørFom = this.opphørFom,
        beslutterIdent = this.beslutterIdent,
    )
