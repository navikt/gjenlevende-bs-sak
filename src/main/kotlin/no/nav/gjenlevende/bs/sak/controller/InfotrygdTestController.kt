package no.nav.gjenlevende.bs.sak.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.gjenlevende.bs.sak.service.InfotrygdClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test/infotrygd")
@Tag(name = "Infotrygd integrasjon test", description = "Endepunkter for å teste integrasjon mot gjenlevende-bs-infotrygd")
class InfotrygdTestController(
    private val infotrygdClient: InfotrygdClient,
) {
    @GetMapping("/ping")
    @Operation(
        summary = "Ping gjenlevende-bs-infotrygd",
        description = "Enkel ping for å verifisere at kall mot gjenlevende-bs-infotrygd fungerer.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Bink! Vi snakket med gjenlvende-bs-infotrygd!",
                content = [Content(schema = Schema(implementation = String::class))],
            ),
            ApiResponse(
                responseCode = "500",
                description = "Bonk! Klarte ikke å nå gjenlevende-bs-infotrygd!",
            ),
        ],
    )
    fun testPing(): ResponseEntity<TestResponse> =
        try {
            val response = infotrygdClient.pingSync()

            ResponseEntity.ok(
                TestResponse(
                    success = true,
                    message = "Klarte å gjøre kall mot gjenlevende-bs-infotrygd!",
                ),
            )
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(
                TestResponse(
                    success = false,
                    message = "Feilet å kalle gjenlvende-bs-infotrygd med melding: ${e.message}",
                ),
            )
        }
}

data class TestResponse(
    @Schema(description = "Om testen var vellykket", example = "true")
    val success: Boolean,
    @Schema(description = "Beskrivelse av resultatet")
    val message: String,
)
