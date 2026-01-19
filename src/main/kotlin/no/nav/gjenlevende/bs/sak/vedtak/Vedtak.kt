package no.nav.gjenlevende.bs.sak.vedtak

import no.nav.gjenlevende.bs.sak.felles.sikkerhet.SikkerhetContext
import no.nav.gjenlevende.bs.sak.felles.sporbar.SporbarUtils
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID

data class Vedtak(
    @Id
    val behandlingId: UUID,
    val resultatType: ResultatType,
    val begrunnelse: String? = null,
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
    val datoFra: LocalDate,
    val datoTil: LocalDate,
    val utgifter: Int,
    val barn: List<UUID>,
    val periodetype: PeriodetypeBarnetilsyn,
    val aktivitetstype: AktivitetstypeBarnetilsyn? = null,
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
