package no.nav.gjenlevende.bs.sak.vedtak

import no.nav.gjenlevende.bs.sak.infrastruktur.exception.Feil
import no.nav.gjenlevende.bs.sak.vedtak.BeregningUtils.beregnBarnetilsynperiode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID
import kotlin.collections.any

@Service
class VedtakService(
    private val vedtakRepository: VedtakRepository,
) {
    fun hentVedtak(behandlingId: UUID): Vedtak? = vedtakRepository.findByIdOrNull(behandlingId)

    fun lagreVedtak(
        vedtakDto: VedtakDto,
        behandlingId: UUID,
    ): UUID = vedtakRepository.insert(vedtakDto.tilVedtak(behandlingId)).behandlingId

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

            val utgifer = barnetilsynperioder.map { periode -> periode.utgifter }
            validerFornuftigeBeløp(utgifer)

            validerAntallBarnOgUtgifter(barnetilsynperioder)
            validerOpphørIkkeFørsteEllerSistePeriode(barnetilsynperioder)
        }
        if (vedtakDto.resultatType == ResultatType.OPPHØR) {
            if (vedtakDto.opphørFom == null) {
                throw Feil("Kan ikke opphøre uten å velge opphørsdato")
            }
            if (vedtakDto.barnetilsynperioder.isNotEmpty()){
                throw Feil("Kan ikke være barnetilsynsperioder på et opphørsvedtak")
            }
        }
        if (vedtakDto.resultatType == ResultatType.AVSLÅTT){
            if (vedtakDto.barnetilsynperioder.isNotEmpty()){
                throw Feil("Kan ikke være barnetilsynsperioder på et opphørsvedtak")
            }
        }
        validerBegrunnelse(vedtakDto)
    }

    fun validerKanBeregne(
        barnetilsynBeregningRequest: BarnetilsynBeregningRequest,
    ) {
        val barnetilsynBeregninger = barnetilsynBeregningRequest.barnetilsynBeregning

        val månedsPerioder = barnetilsynBeregninger.map { periode -> Månedsperiode(periode.datoFra, periode.datoTil) }
        validerGyldigePerioder(månedsPerioder)

        val utgifer = barnetilsynBeregninger.map { periode -> periode.utgifter }
        validerFornuftigeBeløp(utgifer)
    }

    private fun validerGyldigePerioder(
        månedsPerioder: List<Månedsperiode>,
    ) {
        if (månedsPerioder.isEmpty()) {
            throw Feil("Ingen perioder")
        }
        if (månedsPerioder.harOverlappende()) {
            throw Feil("Har overlappende perioder")
        }
        if (!månedsPerioder.erSammenhengende()) {
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
            throw Feil("Utgiter på mer enn 40000 støttes ikke")
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
            throw Feil("Første periode kan ikke være periodetype Ingen stønad")
        }
        if (barnetilsynperioder.last().periodetype == PeriodetypeBarnetilsyn.INGEN_STØNAD) {
            throw Feil("Siste periode kan ikke være periodetype Ingen stønad")
        }
    }

    private fun validerBegrunnelse(
        vedtakDto: VedtakDto,
    ) {
        if (vedtakDto.begrunnelse.isNullOrEmpty()) {
            throw Feil("Mangler begrunnelse")
        }
    }
}
