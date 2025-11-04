package no.nav.gjenlevende.bs.sak.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/status")
@Tag(name = "Status", description = "Status endepunkter")
class StatusController {
    @GetMapping
    @Operation(
        summary = "Hent applikasjon status",
        description = "Returnerer status for applikasjonen, samt versjon og oppetid.",
    )
    fun getStatus(): ResponseEntity<StatusResponse> =
        ResponseEntity.ok(
            StatusResponse(
                status = "UP",
                timestamp = LocalDateTime.now(),
                version = "1.0.0-SNAPSHOT",
                application = "gjenlevende-bs-sak",
            ),
        )

    @GetMapping("/ping")
    @Operation(
        summary = "Ping endepunkt",
        description = "Enkel ping endepunkt som sjekker om applikasjonen lever.",
    )
    fun ping(): ResponseEntity<String> = ResponseEntity.ok("pong")
}

data class StatusResponse(
    @Schema(description = "Applikasjonstatus", example = "UP")
    val status: String,
    @Schema(description = "Nåværendetid", example = "2025-11-03T10:30:00")
    val timestamp: LocalDateTime,
    @Schema(description = "Applikasjonversjon", example = "1.0.0-SNAPSHOT")
    val version: String,
    @Schema(description = "Applikasjonnavn", example = "gjenlevende-bs-sak")
    val application: String,
)
