package no.nav.gjenlevende.bs.sak.saksbehandler

import SaksbehandlerResponse
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.Tilgangskontroll
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class HentSaksbehandlerRequest(
    val navIdent: String,
)

@RestController
@Tilgangskontroll
@PreAuthorize("hasRole('SAKSBEHANDLER')")
@RequestMapping(path = ["/api/saksbehandler"])
class SaksbehandlerController(
    private val entraProxyClient: EntraProxyClient,
) {
    @PostMapping("/hent")
    fun hentSaksbehandler(
        @RequestBody request: HentSaksbehandlerRequest,
    ): ResponseEntity<SaksbehandlerResponse> {
        val saksbehandler = entraProxyClient.hentSaksbehandlerInfo(request.navIdent)
        return ResponseEntity.ok(saksbehandler)
    }
}
