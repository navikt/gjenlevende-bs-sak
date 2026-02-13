package no.nav.gjenlevende.bs.sak.beslutter

import no.nav.gjenlevende.bs.sak.behandling.BehandlingService
import no.nav.gjenlevende.bs.sak.behandling.BehandlingStatus
import no.nav.gjenlevende.bs.sak.brev.BrevService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BeslutterService(
    private val behandlingService: BehandlingService,
    private val brevService: BrevService,
) {
    @Transactional
    fun sendTilBeslutter(behandlingId: UUID) {
        brevService.oppdaterSaksbehandlerForBrev(behandlingId)
        behandlingService.oppdaterBehandlingStatus(
            behandlingId = behandlingId,
            status = BehandlingStatus.FATTER_VEDTAK,
        )
    }

    @Transactional
    fun angreSendTilBeslutter(behandlingId: UUID) {
        behandlingService.oppdaterBehandlingStatus(
            behandlingId = behandlingId,
            status = BehandlingStatus.UTREDES,
        )
    }
}
