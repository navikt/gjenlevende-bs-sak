package no.nav.gjenlevende.bs.sak.endringshistorikk

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EndringshistorikkService(
    private val behandlingEndringRepository: BehandlingEndringRepository,
) {
    fun registrerEndring(
        behandlingId: UUID,
        endringType: EndringType,
        detaljer: String? = null,
    ) {
        behandlingEndringRepository.insert(
            BehandlingEndring(
                behandlingId = behandlingId,
                endringType = endringType,
                detaljer = detaljer,
            ),
        )
    }

    fun hentEndringshistorikk(behandlingId: UUID): List<BehandlingEndring> = behandlingEndringRepository.findByBehandlingIdOrderByUtførtTidDesc(behandlingId)
}
