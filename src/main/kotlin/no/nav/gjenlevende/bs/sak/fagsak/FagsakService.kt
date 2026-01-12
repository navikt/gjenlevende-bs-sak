package no.nav.gjenlevende.bs.sak.fagsak

import no.nav.gjenlevende.bs.sak.fagsak.domain.Fagsak
import no.nav.gjenlevende.bs.sak.fagsak.domain.FagsakPerson
import no.nav.gjenlevende.bs.sak.fagsak.dto.FagsakDto
import no.nav.gjenlevende.bs.sak.fagsak.dto.tilDto
import no.nav.gjenlevende.bs.sak.infotrygd.dto.StønadType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
open class FagsakService(
    private val fagsakRepository: FagsakRepository,
    private val fagsakPersonService: FagsakPersonService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun hentEllerOpprettFagsakMedBehandlinger(
        personident: String,
        stønadstype: StønadType,
    ): FagsakDto {
        val fagsak = hentEllerOpprettFagsak(personident = personident, stønadstype = stønadstype)
        return fagsak.tilDto(personident)
    }

    @Transactional
    fun hentEllerOpprettFagsakMedFagsakPersonId(
        fagsakPersonId: UUID,
        stønadstype: StønadType,
    ): FagsakDto {
        val fagsak = hentEllerOpprettFagsakMedId(fagsakPersonId = fagsakPersonId, stønadstype = stønadstype)
        val personident = fagsakPersonService.hentAktivIdent(fagsakPersonId)

        return fagsak.tilDto(personident)
    }

    @Transactional
    open fun hentEllerOpprettFagsak(
        personident: String,
        stønadstype: StønadType,
    ): Fagsak {
        val fagsakPerson = fagsakPersonService.hentEllerOpprettPerson(setOf(personident), personident)

        return hentEllerOpprettFagsakForPerson(fagsakPerson, stønadstype)
    }

    @Transactional
    open fun hentEllerOpprettFagsakMedId(
        fagsakPersonId: UUID,
        stønadstype: StønadType,
    ): Fagsak {
        val fagsakPerson =
            fagsakPersonService.finnPersonMedId(fagsakPersonId)
                ?: throw IllegalArgumentException("Fant ingen fagsakPerson med id $fagsakPersonId")

        return hentEllerOpprettFagsakForPerson(fagsakPerson, stønadstype)
    }

    private fun hentEllerOpprettFagsakForPerson(
        fagsakPerson: FagsakPerson,
        stønadstype: StønadType,
    ): Fagsak {
        logger.info("FagsakPerson: $fagsakPerson")

        val fagsak = hentFagsakForPerson(fagsakPerson = fagsakPerson, stønadstype = stønadstype) ?: opprettFagsak(fagsakPerson = fagsakPerson, stønadstype = stønadstype)

        return fagsak
    }

    private fun hentFagsakForPerson(
        fagsakPerson: FagsakPerson,
        stønadstype: StønadType,
    ): Fagsak? =
        fagsakRepository.findByFagsakPersonIdAndStønadstype(
            fagsakPerson.id,
            stønadstype,
        )

    private fun opprettFagsak(
        fagsakPerson: FagsakPerson,
        stønadstype: StønadType,
    ): Fagsak =
        fagsakRepository.insert(
            Fagsak(
                fagsakPersonId = fagsakPerson.id,
                stønadstype = stønadstype,
            ),
        )
}
