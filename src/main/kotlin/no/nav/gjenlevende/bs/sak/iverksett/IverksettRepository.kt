package no.nav.gjenlevende.bs.sak.iverksett

import no.nav.gjenlevende.bs.sak.felles.InsertUpdateRepository
import no.nav.gjenlevende.bs.sak.felles.RepositoryInterface
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface IverksettRepository :
    RepositoryInterface<Iverksett, UUID>,
    InsertUpdateRepository<Iverksett>
