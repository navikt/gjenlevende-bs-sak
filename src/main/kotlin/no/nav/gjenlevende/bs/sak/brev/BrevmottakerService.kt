package no.nav.gjenlevende.bs.sak.brev

import no.nav.gjenlevende.bs.sak.behandling.BehandlingService
import no.nav.gjenlevende.bs.sak.brev.domain.Brevmottaker
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BrevmottakerService(
    private val brevmottakerRepository: BrevmottakerRepository,
    private val behandlingService: BehandlingService,
) {
    fun hentBrevmottakere(behandlingId: UUID): List<Brevmottaker> = brevmottakerRepository.findAllByBehandlingId(behandlingId)

    @Transactional
    fun oppdaterBrevmottakere(
        behandlingId: UUID,
        brevmottakere: List<Brevmottaker>,
    ): List<Brevmottaker> {
        behandlingService.validerBehandlingErRedigerbar(behandlingId)
        brevmottakerRepository.deleteAllByBehandlingId(behandlingId)
        return brevmottakerRepository.insertAll(
            brevmottakere.map { it.copy(behandlingId = behandlingId) },
        )
    }
}
