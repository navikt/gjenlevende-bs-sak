package no.nav.gjenlevende.bs.sak.fagsak

import no.nav.gjenlevende.bs.sak.SpringContextTest
import no.nav.gjenlevende.bs.sak.fagsak.domain.FagsakDomain
import no.nav.gjenlevende.bs.sak.fagsak.domain.FagsakPerson
import no.nav.gjenlevende.bs.sak.fagsak.domain.PersonIdent
import no.nav.gjenlevende.bs.sak.infotrygd.dto.StønadType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class FagsakRepositoryTest : SpringContextTest() {
    @Autowired
    private lateinit var fagsakRepository: FagsakRepository

    @Autowired
    private lateinit var fagsakPersonRepository: FagsakPersonRepository

    @Test
    fun `insert fagsak`() {
        val ident = "01010199999"
        val fagsakPerson = fagsakPersonRepository.insert(FagsakPerson(identer = setOf(PersonIdent(ident))))

        fagsakRepository.insert(FagsakDomain(fagsakPersonId = fagsakPerson.id, stønadstype = StønadType.BARNETILSYN))
        val alleFagsaker = fagsakRepository.findAll()

        assertThat(alleFagsaker).hasSize(1)
        val fagsak = alleFagsaker.first()
        assertThat(fagsak.id).isEqualTo(fagsak.id)
        assertThat(fagsak.eksternId).isGreaterThanOrEqualTo(200_000_000)
    }
}
