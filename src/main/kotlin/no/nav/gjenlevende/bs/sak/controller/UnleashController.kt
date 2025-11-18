package no.nav.gjenlevende.bs.sak.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.gjenlevende.bs.sak.service.UnleashService
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
    @GetMapping("/toggles")
    @Operation(summary = "Hent alle feature toggles", description = "Returnerer en map med alle feature toggles og deres status")
    fun getAllToggles(): ResponseEntity<Map<String, Boolean>> = ResponseEntity.ok(unleashService.getAllToggles())
}
