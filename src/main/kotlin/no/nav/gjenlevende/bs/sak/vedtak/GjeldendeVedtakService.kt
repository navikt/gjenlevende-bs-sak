package no.nav.gjenlevende.bs.sak.vedtak

import no.nav.gjenlevende.bs.sak.behandling.BehandlingRepository
import no.nav.gjenlevende.bs.sak.infrastruktur.exception.Feil
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.YearMonth
import java.util.UUID

@Service
class GjeldendeVedtakService(
    private val behandlingRepository: BehandlingRepository,
    private val vedtakRepository: VedtakRepository,
) {
    fun hentGjeldendeVedtakFraDato(
        behandlingId: UUID,
        fra: YearMonth,
    ): VedtakDto {
        val behandling =
            behandlingRepository.findByIdOrNull(behandlingId)
                ?: throw Feil("Fant ikke behandling med id=$behandlingId")

        val alleFerdigstilteBehandlinger =
            behandlingRepository
                .finnAlleIverksatteBehandlinger(behandling.fagsakId)
                ?.sortedBy { it.sporbar.endret.endretTid } ?: throw Feil("Fant ingen ferdigstilte behandlinger med id=$behandlingId")

        val vedtakListe =
            alleFerdigstilteBehandlinger
                .mapNotNull { vedtakRepository.findByBehandlingId(it.id) }

        val sammenslåttPerioder = sammenslåBarnetilsynsperioder(vedtakListe, fra)

        return VedtakDto(
            resultatType = ResultatType.INNVILGET,
            barnetilsynperioder = sammenslåttPerioder,
        )
    }

    private fun sammenslåBarnetilsynsperioder(
        vedtakListe: List<Vedtak>,
        fra: YearMonth,
    ): List<Barnetilsynperiode> {
        val månedTilPeriode = mutableMapOf<YearMonth, MonthPeriodData>()

        for (vedtak in vedtakListe) {
            if (vedtak.resultatType == ResultatType.OPPHØR && vedtak.opphørFom != null) {
                månedTilPeriode.keys.filter { it >= vedtak.opphørFom }.forEach { month ->
                    månedTilPeriode[month] = MonthPeriodData.ingenStønad()
                }
            } else if (vedtak.resultatType == ResultatType.INNVILGET) {
                for (periode in vedtak.barnetilsynperioder) {
                    var gjeldendeDato = periode.datoFra
                    while (gjeldendeDato <= periode.datoTil) {
                        månedTilPeriode[gjeldendeDato] =
                            MonthPeriodData(
                                utgifter = periode.utgifter,
                                barn = periode.barn,
                                periodetype = periode.periodetype,
                                aktivitetstype = periode.aktivitetstype,
                            )
                        gjeldendeDato = gjeldendeDato.plusMonths(1)
                    }
                }
            }
        }

        val filtrerteMåneder = månedTilPeriode.filterKeys { it >= fra }

        return konverterTilBarnetilsynsperioder(filtrerteMåneder)
    }

    private fun konverterTilBarnetilsynsperioder(
        månedTilPeriode: Map<YearMonth, MonthPeriodData>,
    ): List<Barnetilsynperiode> {
        if (månedTilPeriode.isEmpty()) return emptyList()

        val sorterteMåneder = månedTilPeriode.keys.sorted()
        val resultat = mutableListOf<Barnetilsynperiode>()

        var periodeStart = sorterteMåneder.first()
        var dataStart = månedTilPeriode[periodeStart]!!

        for (i in 1 until sorterteMåneder.size) {
            val gjeldendeMåned = sorterteMåneder[i]
            val gjeldendeData = månedTilPeriode[gjeldendeMåned]!!

            val forrigeMåned = sorterteMåneder[i - 1]
            val erSammenhengende = forrigeMåned.plusMonths(1) == gjeldendeMåned
            val harLikData = gjeldendeData == dataStart

            if (!erSammenhengende || !harLikData) {
                resultat.add(dataStart.toBarnetilsynperiode(periodeStart, forrigeMåned))

                if (!erSammenhengende) {
                    val utenVedtakStart = forrigeMåned.plusMonths(1)
                    val utenVedtakSlutt = gjeldendeMåned.minusMonths(1)
                    if (utenVedtakStart <= utenVedtakSlutt) {
                        resultat.add(MonthPeriodData.ingenStønad().toBarnetilsynperiode(utenVedtakStart, utenVedtakSlutt))
                    }
                }

                periodeStart = gjeldendeMåned
                dataStart = gjeldendeData
            }
        }
        resultat.add(dataStart.toBarnetilsynperiode(periodeStart, sorterteMåneder.last()))

        return resultat
    }
}

private data class MonthPeriodData(
    val utgifter: BigDecimal,
    val barn: List<UUID>,
    val periodetype: PeriodetypeBarnetilsyn,
    val aktivitetstype: AktivitetstypeBarnetilsyn?,
) {
    companion object {
        fun ingenStønad() =
            MonthPeriodData(
                utgifter = BigDecimal.ZERO,
                barn = emptyList(),
                periodetype = PeriodetypeBarnetilsyn.INGEN_STØNAD,
                aktivitetstype = null,
            )
    }

    fun toBarnetilsynperiode(
        datoFra: YearMonth,
        datoTil: YearMonth,
    ) = Barnetilsynperiode(
        datoFra = datoFra,
        datoTil = datoTil,
        utgifter = utgifter,
        barn = barn,
        periodetype = periodetype,
        aktivitetstype = aktivitetstype,
    )
}
