package no.nav.gjenlevende.bs.sak.behandling

import no.nav.gjenlevende.bs.sak.felles.sikkerhet.Tilgangskontroll
import no.nav.gjenlevende.bs.sak.infrastruktur.exception.Feil
import org.springframework.http.ResponseEntity
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

data class HentBehandlingerRequest(
    val fagsakId: UUID,
)

@RestController
@RequestMapping(path = ["/api/behandling"])
class BehandlingController(
    private val behandlingService: BehandlingService,
) {
    @PostMapping("/opprett")
    fun opprettBehandling(
        @RequestBody opprettRequest: OpprettRequest,
    ): ResponseEntity<UUID> {
        val fagsakId = opprettRequest.fagsakId

        if (behandlingService.finnesÅpenBehandling(opprettRequest.fagsakId)) {
            throw Feil("Finnes åpen behandling")
        }

        val behandling = behandlingService.opprettBehandling(fagsakId)

        return ResponseEntity.ok(behandling.id)
    }

    @PostMapping("/hentBehandlinger")
    fun hentBehandlinger(
        @RequestBody hentBehandlingerRequest: HentBehandlingerRequest,
    ): ResponseEntity<List<BehandlingDto>> {
        val fagsakId = hentBehandlingerRequest.fagsakId

        val behandlinger = behandlingService.hentBehandlingerFraFagsak(fagsakId)
        return ResponseEntity.ok(behandlinger?.map { it.tilDto() })
    }

    @PostMapping("/hent")
    fun hentBehandling(
        @RequestBody hentRequest: HentRequest,
    ): ResponseEntity<BehandlingDto> {
        val behandlingId = hentRequest.behandlingId

        val behandling = behandlingService.hentBehandling(behandlingId)
        return ResponseEntity.ok(behandling?.tilDto())
    }
}
