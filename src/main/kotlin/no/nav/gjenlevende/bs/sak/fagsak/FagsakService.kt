package no.nav.gjenlevende.bs.sak.fagsak

import no.nav.gjenlevende.bs.sak.fagsak.domain.Fagsak
import no.nav.gjenlevende.bs.sak.fagsak.domain.FagsakDomain
import no.nav.gjenlevende.bs.sak.fagsak.domain.FagsakPerson
import no.nav.gjenlevende.bs.sak.fagsak.domain.tilFagsakMedPerson
import no.nav.gjenlevende.bs.sak.fagsak.dto.FagsakDto
import no.nav.gjenlevende.bs.sak.fagsak.dto.tilDto
import no.nav.gjenlevende.bs.sak.infotrygd.dto.StønadType
import org.apache.catalina.core.ApplicationContext
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
open class FagsakService(
    private val fagsakRepository: FagsakRepository,
    private val fagsakPersonService: FagsakPersonService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun hentEllerOpprettFagsakMedBehandlinger(
        personIdent: String,
        stønadstype: StønadType,
    ): FagsakDto = hentEllerOpprettFagsak(personIdent, stønadstype).tilDto()

    @Transactional
    open fun hentEllerOpprettFagsak(
        personIdent: String,
        stønadstype: StønadType,
    ): Fagsak {
        val fagsakPerson = fagsakPersonService.hentEllerOpprettPerson(setOf(personIdent), personIdent)
        logger.info("FagsakPerson: $fagsakPerson")
        val fagsak = opprettFagsak(fagsakPerson, stønadstype).tilFagsakMedPerson(fagsakPerson.identer)
        logger.info("Fagsak: $fagsak")
        return fagsak
    }

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
