package no.nav.gjenlevende.bs.sak.fagsak

import no.nav.gjenlevende.bs.sak.SpringContextTest
import no.nav.gjenlevende.bs.sak.fagsak.domain.FagsakPerson
import no.nav.gjenlevende.bs.sak.fagsak.domain.PersonIdent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.text.insert

open class FagsakPersonRepositoryTest : SpringContextTest() {
    @Autowired
    private lateinit var fagsakPersonRepository: FagsakPersonRepository

    @Test
    fun `lagre og hent FagsakPerson`() {
        val person1 =
            fagsakPersonRepository.insert(
                FagsakPerson(
                    identer = setOf(PersonIdent("1"), PersonIdent("3")),
                ),
            )
        val person2 = fagsakPersonRepository.insert(FagsakPerson(identer = setOf(PersonIdent("2"))))

        assertThat(fagsakPersonRepository.findPersonIdenter(person1.id)).containsExactlyInAnyOrderElementsOf(person1.identer)
        assertThat(fagsakPersonRepository.findPersonIdenter(person2.id)).containsExactlyInAnyOrderElementsOf(person2.identer)
    }
}
