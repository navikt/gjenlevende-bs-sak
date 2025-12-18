package no.nav.gjenlevende.bs.sak.behandling

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BehandlingService(
    private val behandlingRepository: BehandlingRepository,
) {
    @Transactional
    fun opprettBehandling(
        fagsakId: UUID,
        status: BehandlingStatus = BehandlingStatus.OPPRETTET,
    ): Behandling {
        val behandling =
            Behandling(
                fagsakId = fagsakId,
                status = status,
            )

        behandlingRepository.insert(
            behandling,
        )

        return behandling
    }

    fun hentBehandling(behandlingId: UUID): Behandling? = behandlingRepository.findByIdOrNull(behandlingId)
}
