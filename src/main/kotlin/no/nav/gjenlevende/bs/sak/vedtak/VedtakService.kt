package no.nav.gjenlevende.bs.sak.vedtak

import no.nav.gjenlevende.bs.sak.behandling.BehandlingRepository
import no.nav.gjenlevende.bs.sak.behandling.BehandlingResultat
import no.nav.gjenlevende.bs.sak.behandling.BehandlingStatus
import no.nav.gjenlevende.bs.sak.endringshistorikk.EndringType
import no.nav.gjenlevende.bs.sak.endringshistorikk.EndringshistorikkService
import no.nav.gjenlevende.bs.sak.infrastruktur.exception.Feil
import no.nav.gjenlevende.bs.sak.vedtak.BeregningUtils.beregnBarnetilsynperiode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.YearMonth
import java.util.UUID
import kotlin.collections.any
import kotlin.text.isNullOrEmpty

@Service
class VedtakService(
    private val vedtakRepository: VedtakRepository,
    private val behandlingRepository: BehandlingRepository,
    private val endringshistorikkService: EndringshistorikkService,
) {
    fun hentVedtak(behandlingId: UUID): Vedtak? = vedtakRepository.findByIdOrNull(behandlingId)

    fun lagreVedtak(
        vedtakDto: VedtakDto,
        behandlingId: UUID,
    ): UUID {
        val vedtak = vedtakRepository.insert(vedtakDto.tilVedtak(behandlingId))
        endringshistorikkService.registrerEndring(
            behandlingId = behandlingId,
            endringType = EndringType.VEDTAK_LAGRET,
            detaljer = "Resultat: ${vedtakDto.resultatType}",
        )
        return vedtak.behandlingId
    }

    fun slettVedtakHvisFinnes(behandlingId: UUID) {
        vedtakRepository.deleteById(behandlingId)
    }

    fun lagBeløpsperioder(barnetilsynBeregningRequest: BarnetilsynBeregningRequest): List<BeløpsperioderDto> = beregnBarnetilsynperiode(barnetilsynBeregningRequest.barnetilsynBeregning)

    fun validerKanLagreVedtak(
        vedtakDto: VedtakDto,
    ) {
        if (vedtakDto.resultatType == ResultatType.INNVILGET) {
            val barnetilsynperioder = vedtakDto.barnetilsynperioder

            val månedsPerioder = barnetilsynperioder.map { periode -> Månedsperiode(periode.datoFra, periode.datoTil) }
            validerGyldigePerioder(månedsPerioder)

            val utgifter = barnetilsynperioder.map { periode -> periode.utgifter }
            validerFornuftigeBeløp(utgifter)

            validerAntallBarnOgUtgifter(barnetilsynperioder)
            validerOpphørIkkeFørsteEllerSistePeriode(barnetilsynperioder)
        }
        if (vedtakDto.resultatType == ResultatType.OPPHØR) {
            if (vedtakDto.opphørFom == null) {
                throw Feil("Kan ikke opphøre uten å velge opphørsdato")
            }
            if (vedtakDto.barnetilsynperioder.isNotEmpty()) {
                throw Feil("Kan ikke være barnetilsynsperioder på et opphørsvedtak")
            }
        }
        if (vedtakDto.resultatType == ResultatType.AVSLÅTT) {
            if (vedtakDto.barnetilsynperioder.isNotEmpty()) {
                throw Feil("Kan ikke være barnetilsynsperioder på et opphørsvedtak")
            }
        }
        validerBegrunnelse(vedtakDto)
    }

    fun validerKanBeregne(
        barnetilsynBeregningRequest: BarnetilsynBeregningRequest,
    ) {
        val barnetilsynBeregninger = barnetilsynBeregningRequest.barnetilsynBeregning

        val månedsperioder = barnetilsynBeregninger.map { periode -> Månedsperiode(periode.datoFra, periode.datoTil) }
        validerGyldigePerioder(månedsperioder)

        val utgifter = barnetilsynBeregninger.map { periode -> periode.utgifter }
        validerFornuftigeBeløp(utgifter)
    }

    private fun validerGyldigePerioder(
        månedsperioder: List<Månedsperiode>,
    ) {
        if (månedsperioder.isEmpty()) {
            throw Feil("Ingen perioder")
        }
        if (månedsperioder.harOverlappende()) {
            throw Feil("Har overlappende perioder")
        }
        if (!månedsperioder.erSammenhengende()) {
            throw Feil("Perioder må være sammenhengende")
        }
    }

    private fun validerFornuftigeBeløp(
        utgifter: List<BigDecimal>,
    ) {
        if (utgifter.any({ it.toInt() < 0 })) {
            throw Feil("Utgifter kan ikke være mindre enn 0")
        }
        if (utgifter.any({ it.toInt() > 40000 })) {
            throw Feil("Utgifter på mer enn 40000 støttes ikke")
        }
    }

    private fun validerAntallBarnOgUtgifter(
        barnetilsynperioder: List<Barnetilsynperiode>,
    ) {
        if (barnetilsynperioder.any { it.periodetype == PeriodetypeBarnetilsyn.INGEN_STØNAD && it.barn.isNotEmpty() }) {
            throw Feil("Kan ikke ta med barn på en periode som er av type ingen stønad")
        }
        if (barnetilsynperioder.any { it.periodetype == PeriodetypeBarnetilsyn.INGEN_STØNAD && it.utgifter.toInt() > 0 }) {
            throw Feil("Kan ikke ha utgifter større enn null på en periode som er av type ingen stønad")
        }
        if (barnetilsynperioder.any { it.periodetype == PeriodetypeBarnetilsyn.ORDINÆR && it.barn.isEmpty() }) {
            throw Feil("Må ha med minst et barn på en periode som er ordinær")
        }
        if (barnetilsynperioder.any { it.periodetype == PeriodetypeBarnetilsyn.ORDINÆR && it.utgifter.toInt() <= 0 }) {
            throw Feil("Kan ikke ha null utgifter på en periode som er ordinær")
        }
    }

    private fun validerOpphørIkkeFørsteEllerSistePeriode(
        barnetilsynperioder: List<Barnetilsynperiode>,
    ) {
        if (barnetilsynperioder.first().periodetype == PeriodetypeBarnetilsyn.INGEN_STØNAD) {
            throw Feil("Første periode kan ikke være periodetype ingen stønad")
        }
        if (barnetilsynperioder.last().periodetype == PeriodetypeBarnetilsyn.INGEN_STØNAD) {
            throw Feil("Siste periode kan ikke være periodetype ingen stønad")
        }
    }

    private fun validerBegrunnelse(
        vedtakDto: VedtakDto,
    ) {
        if (vedtakDto.begrunnelse.isNullOrEmpty()) {
            throw Feil("Mangler begrunnelse")
        }
    }

    fun hentVedtakFraDato(
        behandlingId: UUID,
        fra: YearMonth,
    ): VedtakDto {
        val behandling =
            behandlingRepository.findByIdOrNull(behandlingId)
                ?: throw Feil("Fant ikke behandling med id=$behandlingId")

        val alleBehandlinger =
            behandlingRepository
                .findAllByFagsakId(behandling.fagsakId)
                .filter { it.status == BehandlingStatus.FERDIGSTILT }
                .filter { it.resultat == BehandlingResultat.INNVILGET || it.resultat == BehandlingResultat.OPPHØRT }
                .sortedBy { it.sporbar.opprettetTid }

        val vedtakListe =
            alleBehandlinger
                .mapNotNull { vedtakRepository.findByIdOrNull(it.id) }
                .filter { it.resultatType == ResultatType.INNVILGET || it.resultatType == ResultatType.OPPHØR }

        val mergedPerioder = mergeBarnetilsynperioder(vedtakListe, fra)

        return VedtakDto(
            resultatType = ResultatType.INNVILGET,
            barnetilsynperioder = mergedPerioder,
        )
    }

    private fun mergeBarnetilsynperioder(
        vedtakListe: List<Vedtak>,
        fra: YearMonth,
    ): List<Barnetilsynperiode> {
        val månedTilPeriode = mutableMapOf<YearMonth, MonthPeriodData>()

        for (vedtak in vedtakListe) {
            if (vedtak.resultatType == ResultatType.OPPHØR && vedtak.opphørFom != null) {
                månedTilPeriode.keys.filter { it >= vedtak.opphørFom }.forEach { månedTilPeriode.remove(it) }
            } else if (vedtak.resultatType == ResultatType.INNVILGET) {
                for (periode in vedtak.barnetilsynperioder) {
                    var current = periode.datoFra
                    while (current <= periode.datoTil) {
                        månedTilPeriode[current] =
                            MonthPeriodData(
                                utgifter = periode.utgifter,
                                barn = periode.barn,
                                periodetype = periode.periodetype,
                                aktivitetstype = periode.aktivitetstype,
                            )
                        current = current.plusMonths(1)
                    }
                }
            }
        }

        val filteredMonths = månedTilPeriode.filterKeys { it >= fra }

        return convertToBarnetilsynperioder(filteredMonths)
    }

    private fun convertToBarnetilsynperioder(
        månedTilPeriode: Map<YearMonth, MonthPeriodData>,
    ): List<Barnetilsynperiode> {
        if (månedTilPeriode.isEmpty()) return emptyList()

        val sortedMonths = månedTilPeriode.keys.sorted()
        val result = mutableListOf<Barnetilsynperiode>()

        var periodStart = sortedMonths.first()
        var currentData = månedTilPeriode[periodStart]!!

        for (i in 1 until sortedMonths.size) {
            val month = sortedMonths[i]
            val data = månedTilPeriode[month]!!

            val isConsecutive = sortedMonths[i - 1].plusMonths(1) == month
            val isSameData = data == currentData

            if (!isConsecutive || !isSameData) {
                result.add(
                    Barnetilsynperiode(
                        behandlingId = UUID.randomUUID(),
                        datoFra = periodStart,
                        datoTil = sortedMonths[i - 1],
                        utgifter = currentData.utgifter,
                        barn = currentData.barn,
                        periodetype = currentData.periodetype,
                        aktivitetstype = currentData.aktivitetstype,
                    ),
                )
                periodStart = month
                currentData = data
            }
        }

        result.add(
            Barnetilsynperiode(
                behandlingId = UUID.randomUUID(),
                datoFra = periodStart,
                datoTil = sortedMonths.last(),
                utgifter = currentData.utgifter,
                barn = currentData.barn,
                periodetype = currentData.periodetype,
                aktivitetstype = currentData.aktivitetstype,
            ),
        )

        return result
    }
}

private data class MonthPeriodData(
    val utgifter: BigDecimal,
    val barn: List<UUID>,
    val periodetype: PeriodetypeBarnetilsyn,
    val aktivitetstype: AktivitetstypeBarnetilsyn,
)
