package no.nav.gjenlevende.bs.sak.iverksett.utbetaling

import no.nav.gjenlevende.bs.sak.behandling.BehandlingService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
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
    @PostMapping("/{behandlingId}")
    fun simulerForBehandling(
        @PathVariable behandlingId: UUID,
    ): ResponseEntity<Map<String, UUID>> {
        val behandling = behandlingService.hentBehandling(behandlingId)
        requireNotNull(behandling)
        val simuleringId = simuleringService.simuler(behandling)
        return ResponseEntity.accepted().body(mapOf("simuleringId" to simuleringId))
    }

    @GetMapping("/{behandlingId}/resultat")
    fun hentSimuleringsresultat(
        @PathVariable behandlingId: UUID,
    ): ResponseEntity<SimuleringResponse> {
        val simulering =
            simuleringService.hentSimulering(behandlingId)
                ?: return ResponseEntity.notFound().build()

        return when (simulering.status) {
            SimuleringStatus.VENTER -> ResponseEntity.noContent().build()
            SimuleringStatus.FERDIG -> ResponseEntity.ok(simulering.respons)
            SimuleringStatus.FEILET -> ResponseEntity.internalServerError().build()
        }
    }
}
