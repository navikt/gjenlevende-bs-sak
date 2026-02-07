package no.nav.gjenlevende.bs.sak.brev

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.gjenlevende.bs.sak.brev.domain.Brevmottaker
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.Tilgangskontroll
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@Tilgangskontroll
@RequestMapping(path = ["/api/brevmottaker"])
@Tag(name = "BrevmottakerController", description = "Endepunkter for håndtering av brevmottakere")
class BrevmottakerController(
    private val brevmottakerService: BrevmottakerService,
) {
    @PostMapping("/{behandlingId}")
    @PreAuthorize("hasRole('SAKSBEHANDLER')")
    @Operation(
        summary = "Oppdaterer brevmottakere",
        description = "Oppdaterer brevmottakere for gitt behandlingId",
    )
    fun oppdaterBrevmottakere(
        @PathVariable behandlingId: UUID,
        @RequestBody brevmottakere: List<Brevmottaker>,
    ): ResponseEntity<List<Brevmottaker>> {
        val oppdaterteBrevmottakere = brevmottakerService.oppdaterBrevmottakere(behandlingId, brevmottakere)
        return ResponseEntity.ok(oppdaterteBrevmottakere)
    }
}
