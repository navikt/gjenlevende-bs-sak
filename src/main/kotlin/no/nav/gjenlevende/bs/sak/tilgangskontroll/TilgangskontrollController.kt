package no.nav.gjenlevende.bs.sak.tilgangskontroll

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/tilgangskontroll")
@Tag(name = "Tilgangskontroll", description = "Test endpoints for tilgangsmaskin-integrasjon")
class TilgangskontrollController(
    private val tilgangsmaskinClient: TilgangsmaskinClient,
) {
    @GetMapping("/har-tilgang")
    @Operation(
        summary = "Sjekk om ansatt har tilgang til bruker",
        description =
            "Sjekker om innlogget ansatt har tilgang til å behandle sak for oppgitt personident. " +
                "Returnerer true hvis ansatt har tilgang, false hvis tilgang avvises på grunn av " +
                "habilitet, skjerming, geografisk tilknytning etc.",
    )
    fun harTilgang(
        @RequestParam personident: String,
    ): TilgangResponse {
        val ansattId = hentAnsattId()
        val harTilgang = tilgangsmaskinClient.harTilgangTilBruker(personident)

        return TilgangResponse(
            ansattId = ansattId,
            personident = personident,
            harTilgang = harTilgang,
        )
    }

    @PostMapping("/har-tilgang-bulk")
    @Operation(
        summary = "Sjekk om ansatt har tilgang til flere brukere",
        description =
            "Sjekker om innlogget ansatt har tilgang til å behandle saker for oppgitte personidenter. " +
                "Returnerer liste med personidenter for brukere ansatt har tilgang til. Maksimalt 1000 per request.",
    )
    fun harTilgangBulk(
        @RequestBody request: TilgangBulkRequest,
    ): TilgangBulkResponse {
        val ansattId = hentAnsattId()
        val personidenterMedTilgang = tilgangsmaskinClient.harTilgangTilBrukere(request.personidenter)

        return TilgangBulkResponse(
            ansattId = ansattId,
            totalAntall = request.personidenter.size,
            antallMedTilgang = personidenterMedTilgang.size,
            personidenterMedTilgang = personidenterMedTilgang,
        )
    }

    private fun hentAnsattId(): String {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication is JwtAuthenticationToken) {
            return authentication.token.getClaimAsString("NAVident")
                ?: throw IllegalStateException("NAVident claim mangler i token")
        }

        throw IllegalStateException("Ingen JWT authentication funnet")
    }
}

data class TilgangResponse(
    val ansattId: String,
    val personident: String,
    val harTilgang: Boolean,
)

data class TilgangBulkRequest(
    val personidenter: List<String>,
)

data class TilgangBulkResponse(
    val ansattId: String,
    val totalAntall: Int,
    val antallMedTilgang: Int,
    val personidenterMedTilgang: List<String>,
)
