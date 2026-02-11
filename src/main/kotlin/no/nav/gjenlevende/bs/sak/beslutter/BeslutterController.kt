package no.nav.gjenlevende.bs.sak.beslutter

import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.Tilgangskontroll
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@Tilgangskontroll
@PreAuthorize("hasRole('SAKSBEHANDLER')")
@RequestMapping(path = ["/api/beslutter"])
@Tag(name = "BeslutterController", description = "Endepunkter for beslutter-funksjonalitet")
class BeslutterController(
    private val beslutterService: BeslutterService,
) {
    @PostMapping("/send-til-beslutter/{behandlingId}")
    fun sendTilBeslutter(
        @PathVariable behandlingId: UUID,
    ): ResponseEntity<Map<String, String>> {
        beslutterService.sendTilBeslutter(behandlingId)
        return ResponseEntity.ok(mapOf("status" to "OK"))
    }

    @PostMapping("/angre-send-til-beslutter/{behandlingId}")
    fun angreSendTilBeslutter(
        @PathVariable behandlingId: UUID,
    ): ResponseEntity<Map<String, String>> {
        beslutterService.angreSendTilBeslutter(behandlingId)
        return ResponseEntity.ok(mapOf("status" to "OK"))
    }
}
