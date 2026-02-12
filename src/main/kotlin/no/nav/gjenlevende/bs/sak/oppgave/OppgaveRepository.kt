package no.nav.gjenlevende.bs.sak.oppgave

import no.nav.gjenlevende.bs.sak.felles.InsertUpdateRepository
import no.nav.gjenlevende.bs.sak.felles.RepositoryInterface
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OppgaveRepository :
    RepositoryInterface<Oppgave, UUID>,
    InsertUpdateRepository<Oppgave> {
    fun findByBehandlingIdAndType(
        behandlingId: UUID,
        type: String,
    ): Oppgave?
}
