package no.nav.gjenlevende.bs.sak.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.gjenlevende.bs.sak.service.UnleashService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/unleash")
@Tag(name = "Unleash", description = "Feature toggle API")
class UnleashController(
    private val unleashService: UnleashService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/toggles")
    @Operation(summary = "Hent alle feature toggles", description = "Returnerer en map med alle feature toggles og deres status")
    fun getAllToggles(): ResponseEntity<Map<String, Boolean>> {
        val toggles = unleashService.getAllToggles()
        logger.info("Returnerer ${toggles.size} toggles til frontend")
        return ResponseEntity.ok(toggles)
    }

    @GetMapping("/toggles/{toggleName}")
    @Operation(summary = "Sjekk status for en spesifikk toggle", description = "Returnerer true/false for om en feature toggle er aktivert")
    fun isToggleEnabled(
        @PathVariable toggleName: String,
    ): ResponseEntity<ToggleStatus> {
        logger.debug("Sjekker toggle: $toggleName")
        val enabled = unleashService.isEnabled(toggleName)
        return ResponseEntity.ok(ToggleStatus(toggleName, enabled))
    }

    @GetMapping("/test-setup")
    @Operation(summary = "Hent test-setup toggle", description = "Returnerer status for gjenlevende_frontend__test_setup toggle")
    fun getTestSetupToggle(): ResponseEntity<Boolean> {
        val enabled = unleashService.getTestSetupToggle()
        logger.info("Test-setup toggle er: $enabled")
        return ResponseEntity.ok(enabled)
    }
}

data class ToggleStatus(
    val name: String,
    val enabled: Boolean,
)
