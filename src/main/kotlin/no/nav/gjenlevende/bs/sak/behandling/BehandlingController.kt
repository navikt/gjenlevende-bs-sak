package no.nav.gjenlevende.bs.sak.behandling

import no.nav.familie.kontrakter.felles.Ressurs
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class OpprettRequest(
    val fagsakId: UUID,
)

data class HentRequest(
    val behandlingId: UUID,
)

@RestController
@RequestMapping(path = ["/api/behandling"])
class BehandlingController(
    private val behandlingService: BehandlingService,
) {
    @PostMapping("/opprett")
    fun opprettBehandling(
        @RequestBody opprettRequest: OpprettRequest,
    ): Ressurs<UUID> {
        val fagsakId = opprettRequest.fagsakId

        val behandling = behandlingService.opprettBehandling(fagsakId)
        return Ressurs.success(behandling.id)
    }

    @PostMapping("/hent")
    fun hentBehandling(
        @RequestBody hentRequest: HentRequest,
    ): Ressurs<BehandlingDto?> {
        val behandlingId = hentRequest.behandlingId

        val behandling = behandlingService.hentBehandling(behandlingId)
        return Ressurs.success(behandling?.tilDto())
    }
}
