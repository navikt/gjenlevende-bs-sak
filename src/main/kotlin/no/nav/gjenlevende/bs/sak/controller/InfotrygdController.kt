package no.nav.gjenlevende.bs.sak.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.gjenlevende.bs.sak.dto.PersonPerioderResponse
import no.nav.gjenlevende.bs.sak.dto.PersonidentRequest
import no.nav.gjenlevende.bs.sak.service.InfotrygdClient
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test/infotrygd")
@Profile("dev")
@Tag(
    name = "Infotrygd integrasjon test",
    description = "Endepunkter for å teste integrasjon mot gjenlevende-bs-infotrygd",
)
class InfotrygdController(
    private val infotrygdClient: InfotrygdClient,
) {
    private val logger = LoggerFactory.getLogger(InfotrygdController::class.java)

    @PostMapping("/perioder")
    @PreAuthorize("hasRole('SAKSBEHANDLER') and hasRole('BESLUTTER') and hasRole('VEILEDER')")
    @Operation(
        summary = "Hent vedtaksperioder for person fra Infotrygd",
        description = "Henter alle vedtaksperioder for en gitt person basert på personident.",
        security = [SecurityRequirement(name = "oauth2")],
    )
    fun hentPerioderForPerson(
        @RequestBody request: PersonidentRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<PersonPerioderResponse> {
        logger.info("Henter perioder for person fra gjenlevende-bs-infotrygd")

        return try {
            val response =
                infotrygdClient.hentPerioderForPersonSync(
                    brukerToken = jwt.tokenValue,
                    personident = request.personident,
                )

            logger.info("Hentet perioder fra Infotrygd: ${response.barnetilsyn.size} barnetilsyn, ${response.skolepenger.size} skolepenger")
            ResponseEntity.ok(response)
        } catch (exception: Exception) {
            logger.error("Feil ved henting av perioder fra gjenlevende-bs-infotrygd: ${exception.message}", exception)
            throw exception
        }
    }
}
