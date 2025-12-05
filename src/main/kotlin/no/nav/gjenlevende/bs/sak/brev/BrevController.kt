package no.nav.gjenlevende.bs.sak.brev

import com.fasterxml.jackson.databind.JsonNode
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("api/brev")
@ProtectedWithClaims(issuer = "azuread")
@Validated
class BrevController(
    private val brevService: BrevService,
) {
    @PostMapping("/{behandlingId}/{brevMal}")
    fun lagVedtaksbrev(
        @PathVariable behandlingId: UUID,
        @PathVariable brevMal: String,
        @RequestBody brevRequest: JsonNode,
    ): Ressurs<ByteArray> {
        // TODO hent saksbehandling
        // TODO validertilgang til behandling
        // TODO Valider saksbehandlerrolle
        return Ressurs.success(brevService.lagVedtaksbrev(TODO("saksbehandling"), TODO("brevrequest"), brevMal))
    }
}
