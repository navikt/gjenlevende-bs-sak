package no.nav.gjenlevende.bs.sak.behandling.årsak

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ÅrsakBehandlingService(
    private val årsakBehandlingRepository: ÅrsakBehandlingRepository,
) {
    fun hentÅrsakBehandling(behandlingId: UUID): ÅrsakBehandling? = årsakBehandlingRepository.findById(behandlingId).orElse(null)

    fun lagreÅrsakForBehandling(
        behandlingId: UUID,
        årsakBehandlingRequest: ÅrsakBehandlingRequest,
    ): ÅrsakBehandling {
        val eksisterendeÅrsak = hentÅrsakBehandling(behandlingId)

        if (eksisterendeÅrsak == null) {
            return årsakBehandlingRepository.insert(
                ÅrsakBehandling(
                    behandlingId = behandlingId,
                    kravdato = årsakBehandlingRequest.kravdato,
                    årsak = årsakBehandlingRequest.årsak,
                    beskrivelse = årsakBehandlingRequest.beskrivelse,
                ),
            )
        }

        return årsakBehandlingRepository.update(
            eksisterendeÅrsak.copy(
                kravdato = årsakBehandlingRequest.kravdato,
                årsak = årsakBehandlingRequest.årsak,
                beskrivelse = årsakBehandlingRequest.beskrivelse,
            ),
        )
    }
}
