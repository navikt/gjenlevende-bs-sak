package no.nav.gjenlevende.bs.sak.behandling.årsak

import no.nav.gjenlevende.bs.sak.behandling.BehandlingService
import no.nav.gjenlevende.bs.sak.behandling.BehandlingStatus
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ÅrsakBehandlingService(
    private val årsakBehandlingRepository: ÅrsakBehandlingRepository,
    private val behandlingService: BehandlingService,
) {
    fun hentÅrsakBehandling(behandlingId: UUID): ÅrsakBehandling? = årsakBehandlingRepository.findById(behandlingId).orElse(null)

    fun lagreÅrsakForBehandling(
        behandlingId: UUID,
        årsakBehandlingRequest: ÅrsakBehandlingRequest,
    ): ÅrsakBehandling {
        val eksisterendeÅrsak = hentÅrsakBehandling(behandlingId)

        if (eksisterendeÅrsak == null) {
            behandlingService.oppdaterBehandlingStatus(
                behandlingId = behandlingId,
                status = BehandlingStatus.UTREDES,
            )

            return årsakBehandlingRepository.insert(
                ÅrsakBehandling(
                    behandlingId = behandlingId,
                    kravdato = årsakBehandlingRequest.kravdato,
                    årsak = årsakBehandlingRequest.årsak,
                    beskrivelse = årsakBehandlingRequest.beskrivelse,
                ),
            )
        }

        val oppdatert =
            årsakBehandlingRepository.update(
                eksisterendeÅrsak.copy(
                    kravdato = årsakBehandlingRequest.kravdato,
                    årsak = årsakBehandlingRequest.årsak,
                    beskrivelse = årsakBehandlingRequest.beskrivelse,
                ),
            )

        behandlingService.oppdaterEndretTidspunkt(behandlingId)
        return oppdatert
    }
}
