package no.nav.gjenlevende.bs.sak.behandling.årsak

import no.nav.gjenlevende.bs.sak.behandling.BehandlingService
import no.nav.gjenlevende.bs.sak.behandling.BehandlingStatus
import no.nav.gjenlevende.bs.sak.endringshistorikk.EndringType
import no.nav.gjenlevende.bs.sak.endringshistorikk.EndringshistorikkService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ÅrsakBehandlingService(
    private val årsakBehandlingRepository: ÅrsakBehandlingRepository,
    private val behandlingService: BehandlingService,
    private val endringshistorikkService: EndringshistorikkService,
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

            val årsak =
                årsakBehandlingRepository.insert(
                    ÅrsakBehandling(
                        behandlingId = behandlingId,
                        kravdato = årsakBehandlingRequest.kravdato,
                        årsak = årsakBehandlingRequest.årsak,
                        beskrivelse = årsakBehandlingRequest.beskrivelse,
                    ),
                )
            endringshistorikkService.registrerEndring(
                behandlingId = behandlingId,
                endringType = EndringType.ÅRSAK_LAGRET,
                detaljer = "Årsak: ${årsakBehandlingRequest.årsak}",
            )
            return årsak
        }

        val oppdatert =
            årsakBehandlingRepository.update(
                eksisterendeÅrsak.copy(
                    kravdato = årsakBehandlingRequest.kravdato,
                    årsak = årsakBehandlingRequest.årsak,
                    beskrivelse = årsakBehandlingRequest.beskrivelse,
                ),
            )
        endringshistorikkService.registrerEndring(
            behandlingId = behandlingId,
            endringType = EndringType.ÅRSAK_OPPDATERT,
            detaljer = "Årsak: ${årsakBehandlingRequest.årsak}",
        )
        return oppdatert
    }
}
