package no.nav.gjenlevende.bs.sak.tilgangskontroll

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.SikkerhetContext
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/tilgangsmaskin"])
@Tag(name = "Tilgangsmaskin", description = "Endepunkter for testing av tilgangskontroll via Tilgangsmaskinen")
class TilgangsmaskinController(
    private val tilgangsmaskinClient: TilgangsmaskinClient,
) {
    private val logger = LoggerFactory.getLogger(TilgangsmaskinController::class.java)

    @Operation(
        summary = "Sjekk tilgang til en enkelt bruker",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Tilgangssjekk utført",
                content = [Content(schema = Schema(implementation = EnkelTilgangsResponse::class))],
            ),
            ApiResponse(responseCode = "401", description = "Ikke autentisert"),
            ApiResponse(responseCode = "500", description = "Feil ved kommunikasjon med tilgangsmaskinen"),
        ],
    )
    @GetMapping("/sjekk/{brukerId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sjekkTilgangEnkel(
        @Parameter(description = "Personident (11 siffer) for brukeren som skal sjekkes")
        @PathVariable brukerId: String,
    ): EnkelTilgangsResponse {
        val ansattId = SikkerhetContext.hentSaksbehandler()
        logger.info("Sjekker tilgang for saksbehandler $ansattId til bruker")
        return tilgangsmaskinClient.sjekkTilgangEnkel(ansattId, brukerId)
    }

    @Operation(
        summary = "Bulk-sjekk tilgang til flere brukere",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Bulk-sjekk utført",
                content = [Content(schema = Schema(implementation = BulkTilgangsResponse::class))],
            ),
            ApiResponse(responseCode = "401", description = "Ikke autentisert"),
            ApiResponse(responseCode = "500", description = "Feil ved kommunikasjon med tilgangsmaskinen"),
        ],
    )
    @PostMapping("/sjekk/bulk", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sjekkTilgangBulk(
        @Parameter(description = "Regeltype for tilgangssjekk")
        @RequestParam(defaultValue = "KOMPLETT_REGELTYPE") regelType: RegelType,
        @RequestBody request: BulkTilgangsRequest,
    ): BulkTilgangsResponse {
        val ansattId = SikkerhetContext.hentSaksbehandler()
        logger.info("Bulk-sjekker tilgang for saksbehandler $ansattId til ${request.brukerIdenter.size} brukere")
        return tilgangsmaskinClient.sjekkTilgangBulk(ansattId, request.brukerIdenter, regelType)
    }

    @Operation(
        summary = "Hent ansattinformasjon",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Ansattinformasjon hentet",
                content = [Content(schema = Schema(implementation = AnsattInfoResponse::class))],
            ),
            ApiResponse(responseCode = "401", description = "Ikke autentisert"),
            ApiResponse(responseCode = "500", description = "Feil ved kommunikasjon med tilgangsmaskinen"),
        ],
    )
    @GetMapping("/ansatt/{ansattId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentAnsattInfo(
        @Parameter(description = "NAV-ident for ansatt (f.eks. Z990227)")
        @PathVariable ansattId: String,
    ): AnsattInfoResponse {
        logger.info("Henter info for ansatt $ansattId")
        return tilgangsmaskinClient.sjekkAnsatt(ansattId)
    }
}
