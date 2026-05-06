package no.nav.gjenlevende.bs.sak.iverksett.utbetaling

import no.nav.gjenlevende.bs.sak.behandling.BehandlingService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(path = ["/api/simulering"])
@PreAuthorize("hasRole('SAKSBEHANDLER')")
class SimuleringController(
    private val behandlingService: BehandlingService,
    private val simuleringService: SimuleringService,
) {
    @GetMapping("/{behandlingId}")
    fun simulerForBehandling(
        @PathVariable behandlingId: UUID,
    ): ResponseEntity<String> {
        val behandling = behandlingService.hentBehandling(behandlingId)
        requireNotNull(behandling)
        simuleringService.simuler(behandling)
        return ResponseEntity.ok("Utbetalingsmelding sendt til simulering")
    }
}
