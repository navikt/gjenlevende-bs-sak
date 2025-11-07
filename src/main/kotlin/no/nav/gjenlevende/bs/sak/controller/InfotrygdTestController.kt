package no.nav.gjenlevende.bs.sak.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.gjenlevende.bs.sak.service.InfotrygdClient
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
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

    @GetMapping("/uautentisert")
    @Operation(
        summary = "Test uten autentisering",
        description = "Endepunkt som ikke krever autentisering - kan kalles uten innlogging.",
    )
    fun testUtenAuth(): ResponseEntity<Map<String, String>> =
        ResponseEntity.ok(
            mapOf(
                "melding" to "Dette endepunktet krever ikke autentisering",
                "tidspunkt" to java.time.Instant.now().toString(),
            ),
        )

    @GetMapping("/autentisert")
    @Operation(
        summary = "Test med autentisering",
        description = "Endepunkt som krever gyldig JWT token - returnerer informasjon om innlogget bruker.",
        security = [SecurityRequirement(name = "oauth2")],
    )
    fun testMedAuth(
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<BrukerInfo> {
        val navIdent = jwt.getClaimAsString("NAVident") ?: "Ukjent"
        val navn = jwt.getClaimAsString("name") ?: "Ukjent"
        val epost = jwt.getClaimAsString("preferred_username") ?: jwt.getClaimAsString("email") ?: "Ukjent"
        val grupper = jwt.getClaimAsStringList("groups") ?: emptyList()

        return ResponseEntity.ok(
            BrukerInfo(
                navIdent = navIdent,
                navn = navn,
                epost = epost,
                grupper = grupper,
                tokenUtstedtTid = jwt.issuedAt?.toString() ?: "Ukjent",
                tokenUtløperTid = jwt.expiresAt?.toString() ?: "Ukjent",
            ),
        )
    }
}

data class BrukerInfo(
    val navIdent: String,
    val navn: String,
    val epost: String,
    val grupper: List<String>,
    val tokenUtstedtTid: String,
    val tokenUtløperTid: String,
)
