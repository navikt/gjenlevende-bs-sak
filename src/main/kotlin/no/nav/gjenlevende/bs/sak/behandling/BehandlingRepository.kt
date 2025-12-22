package no.nav.gjenlevende.bs.sak.behandling

import no.nav.gjenlevende.bs.sak.felles.InsertUpdateRepository
import no.nav.gjenlevende.bs.sak.felles.RepositoryInterface
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BehandlingRepository :
    RepositoryInterface<Behandling, UUID>,
    InsertUpdateRepository<Behandling>
