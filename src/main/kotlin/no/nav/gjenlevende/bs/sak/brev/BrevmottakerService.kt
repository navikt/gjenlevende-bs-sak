package no.nav.gjenlevende.bs.sak.brev

import no.nav.gjenlevende.bs.sak.brev.domain.Brevmottaker
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BrevmottakerService(
    private val brevmottakerRepository: BrevmottakerRepository,
) {
    fun hentBrevmottakere(behandlingId: UUID): List<Brevmottaker> = brevmottakerRepository.findAllByBehandlingId(behandlingId)

    @Transactional
    fun oppdaterBrevmottakere(
        behandlingId: UUID,
        brevmottakere: List<Brevmottaker>,
    ): List<Brevmottaker> {
        brevmottakerRepository.deleteAllByBehandlingId(behandlingId)
        return brevmottakerRepository.insertAll(
            brevmottakere.map { it.copy(behandlingId = behandlingId) },
        )
    }
}
