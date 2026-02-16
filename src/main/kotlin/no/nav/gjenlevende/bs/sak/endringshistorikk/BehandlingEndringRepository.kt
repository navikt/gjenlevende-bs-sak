package no.nav.gjenlevende.bs.sak.endringshistorikk

import no.nav.gjenlevende.bs.sak.felles.InsertUpdateRepository
import no.nav.gjenlevende.bs.sak.felles.RepositoryInterface
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BehandlingEndringRepository :
    RepositoryInterface<BehandlingEndring, UUID>,
    InsertUpdateRepository<BehandlingEndring> {
    fun findByBehandlingIdOrderByUtførtTidDesc(behandlingId: UUID): List<BehandlingEndring>
}
