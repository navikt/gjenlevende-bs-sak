package no.nav.gjenlevende.bs.sak.behandling

import no.nav.gjenlevende.bs.sak.felles.sporbar.Sporbar
import org.springframework.data.annotation.Id
import java.util.UUID

data class Behandling(
    @Id
    val id: UUID = UUID.randomUUID(),
    val fagsakId: UUID,
    val status: BehandlingStatus,
    val sporbar: Sporbar = Sporbar(),
)

enum class BehandlingStatus {
    OPPRETTET,
    UTREDES,
    FERDIGSTILT,
}
