package no.nav.gjenlevende.bs.sak.behandling.årsak

import no.nav.gjenlevende.bs.sak.fagsak.FagsakController
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

data class ÅrsakBehandlingRequest(
    val kravdato: LocalDate,
    val årsak: Årsak,
    val beskrivelse: String,
)

@RestController
@RequestMapping("/årsak")
class ÅrsakBehandlingController(
    private val årsakBehandlingService: ÅrsakBehandlingService,
) {
    private val logger = LoggerFactory.getLogger(FagsakController::class.java)

    @GetMapping("/{behandlingId}")
    fun hentÅrsakForBehandling(
        @PathVariable behandlingId: UUID,
    ): ResponseEntity<ÅrsakBehandlingDto?> {
        logger.info("kaller hentÅrsakForBehandling for behandlingId: $behandlingId")
        return årsakBehandlingService.hentÅrsakForBehandling(behandlingId)?.tilDto()
    }

    @PostMapping("/{behandlingId}")
    fun lagreÅrsakForBehandling(
        @PathVariable behandlingId: UUID,
        @RequestBody årsakBehandlingRequest: ÅrsakBehandlingRequest,
    ): ResponseEntity<ÅrsakBehandlingDto> {
        logger.info("Lagrer årsak for behandling: $behandlingId")
        val årsak = årsakBehandlingService.lagreÅrsakForBehandling(behandlingId, årsakBehandlingRequest)
        return ResponseEntity.ok(årsak.tilDto())
    }
}
