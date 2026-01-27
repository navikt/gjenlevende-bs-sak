package no.nav.gjenlevende.bs.sak.behandling.årsak

import no.nav.gjenlevende.bs.sak.fagsak.FagsakController
import no.nav.gjenlevende.bs.sak.infrastruktur.exception.Feil
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
    fun hentÅrsakBehandling(
        @PathVariable behandlingId: UUID,
    ): ResponseEntity<ÅrsakBehandlingDto> {
        logger.info("kaller hentÅrsakForBehandling for behandlingId: $behandlingId")
        val årsak = årsakBehandlingService.hentÅrsakBehandling(behandlingId)

        if (årsak == null) {
            throw Feil("Finner ingen årsak for behandling med id: $behandlingId")
        }

        return ResponseEntity.ok(årsak.tilDto())
    }

    @PostMapping("/{behandlingId}")
    fun lagreÅrsakBehandling(
        @PathVariable behandlingId: UUID,
        @RequestBody årsakBehandlingRequest: ÅrsakBehandlingRequest,
    ): ResponseEntity<ÅrsakBehandlingDto> {
        logger.info("Lagrer årsak for behandling: $behandlingId")
        val årsak = årsakBehandlingService.lagreÅrsakForBehandling(behandlingId, årsakBehandlingRequest)
        return ResponseEntity.ok(årsak.tilDto())
    }
}
