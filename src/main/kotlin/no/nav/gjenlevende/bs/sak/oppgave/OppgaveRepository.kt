package no.nav.gjenlevende.bs.sak.oppgave

import no.nav.gjenlevende.bs.sak.felles.InsertUpdateRepository
import no.nav.gjenlevende.bs.sak.felles.RepositoryInterface
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OppgaveRepository :
    RepositoryInterface<OppgaveEntity, UUID>,
    InsertUpdateRepository<OppgaveEntity> {
    fun findByBehandlingIdAndType(
        behandlingId: UUID,
        type: String,
    ): OppgaveEntity?
}
