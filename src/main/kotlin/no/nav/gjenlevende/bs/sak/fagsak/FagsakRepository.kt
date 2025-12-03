package no.nav.gjenlevende.bs.sak.fagsak

import no.nav.gjenlevende.bs.sak.fagsak.domain.FagsakDomain
import no.nav.gjenlevende.bs.sak.felles.InsertUpdateRepository
import no.nav.gjenlevende.bs.sak.felles.RepositoryInterface
import no.nav.gjenlevende.bs.sak.infotrygd.dto.StønadType
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FagsakRepository :
    RepositoryInterface<FagsakDomain, UUID>,
    InsertUpdateRepository<FagsakDomain> {
    @Query("SELECT * FROM fagsak WHERE fagsak_person_id = :fagsakPersonId AND stonadstype = :stønadstype")
    fun findByFagsakPersonIdAndStønadstype(
        fagsakPersonId: UUID,
        stønadstype: StønadType,
    ): FagsakDomain?
}
