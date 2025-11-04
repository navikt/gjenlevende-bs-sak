package no.nav.gjenlevende.bs.sak.controller

import io.swagger.v3.oas.annotations.Operation
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
    fun testPing(): ResponseEntity<String> =
        try {
            val response = infotrygdClient.pingSync()

            ResponseEntity.ok(
                response,
            )
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(
                "Feilet kall mot gjenlevende-bs-infotrygd: ${e.message}",
            )
        }
}
