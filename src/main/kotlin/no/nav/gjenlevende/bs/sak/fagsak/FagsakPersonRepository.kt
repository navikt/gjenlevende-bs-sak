package no.nav.gjenlevende.bs.sak.fagsak

import no.nav.gjenlevende.bs.sak.fagsak.domain.FagsakPerson
import no.nav.gjenlevende.bs.sak.fagsak.domain.PersonIdent
import no.nav.gjenlevende.bs.sak.felles.InsertUpdateRepository
import no.nav.gjenlevende.bs.sak.felles.RepositoryInterface
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FagsakPersonRepository :
    RepositoryInterface<FagsakPerson, UUID>,
    InsertUpdateRepository<FagsakPerson> {
    @Query("SELECT * FROM person_ident WHERE fagsak_person_id = :personId")
    fun findPersonIdenter(personId: UUID): Set<PersonIdent>
}
