package no.nav.gjenlevende.bs.sak.fagsak

import no.nav.gjenlevende.bs.sak.SpringContextTest
import no.nav.gjenlevende.bs.sak.fagsak.domain.FagsakDomain
import no.nav.gjenlevende.bs.sak.infotrygd.dto.StønadType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

class FagsakRepositoryTest : SpringContextTest() {
    @Autowired private lateinit var fagsakRepository: FagsakRepository

    @Test
    fun `insert fagsak`() {
        fagsakRepository.insert(FagsakDomain(fagsakPersonId = UUID.randomUUID(), stønadstype = StønadType.BARNETILSYN))
        val alleFagsaker = fagsakRepository.findAll()
        assertThat(alleFagsaker).hasSize(1)
    }
}
