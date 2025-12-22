package no.nav.gjenlevende.bs.sak.fagsak

import no.nav.gjenlevende.bs.sak.fagsak.domain.Fagsak
import no.nav.gjenlevende.bs.sak.fagsak.domain.FagsakDomain
import no.nav.gjenlevende.bs.sak.fagsak.domain.FagsakPerson
import no.nav.gjenlevende.bs.sak.fagsak.domain.tilFagsakMedPerson
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
        personIdent: String,
        stønadstype: StønadType,
    ): FagsakDto = hentEllerOpprettFagsak(personIdent, stønadstype).tilDto()

    @Transactional
    fun hentEllerOpprettFagsakMedFagsakPersonId(
        fagsakPersonId: UUID,
        stønadstype: StønadType,
    ): FagsakDto = hentEllerOpprettFagsakMedId(fagsakPersonId, stønadstype).tilDto()

    @Transactional
    open fun hentEllerOpprettFagsak(
        personIdent: String,
        stønadstype: StønadType,
    ): Fagsak {
        val fagsakPerson = fagsakPersonService.hentEllerOpprettPerson(setOf(personIdent), personIdent)

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

        val fagsakDomain =
            hentFagsakForPerson(fagsakPerson, stønadstype) ?: opprettFagsak(fagsakPerson, stønadstype)

        return fagsakDomain.tilFagsakMedPerson(fagsakPerson.identer)
    }

    private fun hentFagsakForPerson(
        fagsakPerson: FagsakPerson,
        stønadstype: StønadType,
    ): FagsakDomain? =
        fagsakRepository.findByFagsakPersonIdAndStønadstype(
            fagsakPerson.id,
            stønadstype,
        )

    private fun opprettFagsak(
        fagsakPerson: FagsakPerson,
        stønadstype: StønadType,
    ): FagsakDomain =
        fagsakRepository.insert(
            FagsakDomain(
                fagsakPersonId = fagsakPerson.id,
                stønadstype = stønadstype,
            ),
        )
}
