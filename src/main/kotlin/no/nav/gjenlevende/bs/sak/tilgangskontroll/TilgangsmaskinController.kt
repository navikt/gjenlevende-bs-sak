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
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
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

    @Operation(summary = "Sjekk tilgang til en enkelt bruker",)
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Tilgangssjekk utført",
                content = [Content(schema = Schema(implementation = EnkelTilgangsResponse::class))],
            ),
        ],
    )
    @PostMapping("/sjekk", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sjekkTilgangEnkel(
        @RequestBody request: TilgangssjekkRequest,
    ): EnkelTilgangsResponse {
        val navIdent = SikkerhetContext.hentSaksbehandler()
        logger.info("Sjekker tilgang for saksbehandler $navIdent til bruker")
        return tilgangsmaskinClient.sjekkTilgangEnkel(navIdent, request.personident)
    }

    @Operation(
        summary = "Bulk-sjekk tilgang til flere brukere (forenklet)",
        description = "Sjekker kjerneregelsett (habilitet) og returnerer forenklet respons med harTilgang-flagg",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Bulk-sjekk utført",
                content = [Content(schema = Schema(implementation = ForenkletBulkTilgangsResponse::class))],
            ),
        ],
    )
    @PostMapping("/sjekk/bulk", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sjekkTilgangBulkForenklet(
        @RequestBody request: BulkTilgangsRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ForenkletBulkTilgangsResponse {
        val navIdent = SikkerhetContext.hentSaksbehandler()
        logger.info("Bulk-sjekker tilgang (kjerne) for saksbehandler $navIdent til ${request.personidenter.size} brukere")
        val respons =
            tilgangsmaskinClient.sjekkTilgangBulk(
                brukerToken = jwt.tokenValue,
                personidenter = request.personidenter,
                regelType = RegelType.KJERNE_REGELTYPE,
            )
        return tilForenkletRespons(respons)
    }

    @Operation(
        summary = "Bulk-sjekk tilgang til flere brukere (komplett)",
        description = "Sjekker komplett regelsett inkludert geografiske begrensninger. Returnerer full respons med alle detaljer.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Bulk-sjekk utført",
                content = [Content(schema = Schema(implementation = BulkTilgangsResponse::class))],
            )
        ],
    )
    @PostMapping("/sjekk/bulk/komplett", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sjekkTilgangBulkKomplett(
        @RequestBody request: BulkTilgangsRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): BulkTilgangsResponse {
        val navIdent = SikkerhetContext.hentSaksbehandler()
        logger.info("Bulk-sjekker tilgang (komplett) for saksbehandler $navIdent til ${request.personidenter.size} brukere")
        return tilgangsmaskinClient.sjekkTilgangBulk(
            brukerToken = jwt.tokenValue,
            personidenter = request.personidenter,
            regelType = RegelType.KOMPLETT_REGELTYPE,
        )
    }

    private fun tilForenkletRespons(respons: BulkTilgangsResponse) =
        ForenkletBulkTilgangsResponse(
            navIdent = respons.navIdent,
            resultater =
                respons.resultater.map { resultat ->
                    val harTilgang = resultat.status == 204
                    val detaljer = resultat.detaljer as? Map<*, *>

                    ForenkletTilgangsResultat(
                        personident = resultat.personident,
                        harTilgang = harTilgang,
                        avvisningskode =
                            if (harTilgang) {
                                null
                            } else {
                                detaljer
                                    ?.get("title")
                                    ?.toString()
                                    ?.let { runCatching { Avvisningskode.valueOf(it) }.getOrNull() }
                            },
                        begrunnelse = if (harTilgang) null else detaljer?.get("begrunnelse")?.toString(),
                    )
                },
        )

    @Operation(summary = "Hent ansattinformasjon")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Ansattinformasjon hentet",
                content = [Content(schema = Schema(implementation = AnsattInfoResponse::class))],
            )
        ],
    )
    @PostMapping("/ansatt", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentAnsattInfo(
        @RequestBody request: AnsattInfoRequest,
    ): AnsattInfoResponse {
        logger.info("Henter info for ansatt ${request.navIdent}")
        return tilgangsmaskinClient.sjekkAnsatt(request.navIdent)
    }
}
