package no.nav.gjenlevende.bs.sak.vedtak

import no.nav.gjenlevende.bs.sak.felles.sikkerhet.SikkerhetContext
import no.nav.gjenlevende.bs.sak.felles.sporbar.SporbarUtils
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.MappedCollection
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
    val utgifter: Int,
    val barn: List<UUID>,
    val periodetype: PeriodetypeBarnetilsyn,
    val aktivitetstype: AktivitetstypeBarnetilsyn,
)

enum class ResultatType {
    INNVILGET,
    AVSLÅTT,
    HENLAGT,
    OPPHØR,
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
