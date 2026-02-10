package no.nav.gjenlevende.bs.sak.beslutter

import no.nav.gjenlevende.bs.sak.behandling.Behandling
import no.nav.gjenlevende.bs.sak.behandling.BehandlingRepository
import no.nav.gjenlevende.bs.sak.behandling.BehandlingStatus
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BeslutterService(
    private val behandlingRepository: BehandlingRepository,
) {
    @Transactional
    fun sendTilBeslutter(behandlingId: UUID): Behandling {
        val behandling =
            behandlingRepository.findByIdOrNull(behandlingId)
                ?: error("Fant ikke behandling med id=$behandlingId")

        val oppdatertBehandling = behandling.copy(status = BehandlingStatus.FATTER_VEDTAK)
        return behandlingRepository.update(oppdatertBehandling)
    }
}
