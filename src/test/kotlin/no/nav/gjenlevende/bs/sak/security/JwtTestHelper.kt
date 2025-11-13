package no.nav.gjenlevende.bs.sak.security

import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant
import java.time.temporal.ChronoUnit

object JwtTestHelper {
    private const val TEST_ISSUER = "https://login.microsoftonline.com/test-tenant/v2.0"
    private const val TEST_SUBJECT = "test-subject-123"
    private const val TEST_AUDIENCE = "test-client-id"

    private const val AZURE_GROUP_ID_SAKSBEHANDLER = "5357fbfa-de25-4d23-86a6-f67caf8ddd63"
    private const val AZURE_GROUP_ID_BESLUTTER = "fda781b0-b82c-4049-919d-3b05623f05fb"
    private const val AZURE_GROUP_ID_VEILEDER = "0291bb72-71fa-4a35-9947-ea7b73f09ab8"

    fun opprettGyldigToken(
        navIdent: String = "A123456",
        azureGrupper: List<String> = listOf(AZURE_GROUP_ID_SAKSBEHANDLER),
        navn: String = "Bink Bonk",
        epost: String = "Bink.Bonk@nav.no",
        utløperOm: Long = 3600,
    ): Jwt {
        val nå = Instant.now()
        val utløper = nå.plus(utløperOm, ChronoUnit.SECONDS)

        val headers =
            mapOf(
                "alg" to "RS256",
                "typ" to "JWT",
                "kid" to "test-key-id",
            )

        return Jwt
            .withTokenValue("test-token-value")
            .headers { h -> h.putAll(headers) }
            .issuedAt(nå)
            .expiresAt(utløper)
            .notBefore(nå)
            .issuer(TEST_ISSUER)
            .subject(TEST_SUBJECT)
            .audience(listOf(TEST_AUDIENCE))
            .claim("name", navn)
            .claim("preferred_username", epost)
            .claim("NAVident", navIdent)
            .claim("groups", azureGrupper)
            .build()
    }

    fun opprettTokenUtenNavIdent(): Jwt {
        val nå = Instant.now()
        val utløper = nå.plus(3600, ChronoUnit.SECONDS)

        val headers =
            mapOf(
                "alg" to "RS256",
                "typ" to "JWT",
            )

        return Jwt
            .withTokenValue("test-token-value")
            .headers { header -> header.putAll(headers) }
            .issuedAt(nå)
            .expiresAt(utløper)
            .issuer(TEST_ISSUER)
            .subject(TEST_SUBJECT)
            .audience(listOf(TEST_AUDIENCE))
            .claim("groups", listOf(AZURE_GROUP_ID_SAKSBEHANDLER))
            .build()
    }

    fun opprettTokenUtenGrupper(): Jwt {
        val nå = Instant.now()
        val utløper = nå.plus(3600, ChronoUnit.SECONDS)

        val headers =
            mapOf(
                "alg" to "RS256",
                "typ" to "JWT",
            )

        return Jwt
            .withTokenValue("test-token-value")
            .headers { header -> header.putAll(headers) }
            .issuedAt(nå)
            .expiresAt(utløper)
            .issuer(TEST_ISSUER)
            .subject(TEST_SUBJECT)
            .audience(listOf(TEST_AUDIENCE))
            .claim("NAVident", "A123456")
            .build()
    }

    fun opprettTokenMedUgyldigeGrupper(): Jwt {
        val nå = Instant.now()
        val utløper = nå.plus(3600, ChronoUnit.SECONDS)

        val headers =
            mapOf(
                "alg" to "RS256",
                "typ" to "JWT",
            )

        return Jwt
            .withTokenValue("test-token-value")
            .headers { header -> header.putAll(headers) }
            .issuedAt(nå)
            .expiresAt(utløper)
            .issuer(TEST_ISSUER)
            .subject(TEST_SUBJECT)
            .audience(listOf(TEST_AUDIENCE))
            .claim("NAVident", "A123456")
            .claim("groups", listOf("00000000-0000-0000-0000-000000000000")) // Ukjent AD gruppe
            .build()
    }

    fun opprettSaksbehandlerToken(navIdent: String = "A123456"): Jwt =
        opprettGyldigToken(
            navIdent = navIdent,
            azureGrupper = listOf(AZURE_GROUP_ID_SAKSBEHANDLER),
        )

    fun opprettBeslutterToken(navIdent: String = "B123456"): Jwt =
        opprettGyldigToken(
            navIdent = navIdent,
            azureGrupper = listOf(AZURE_GROUP_ID_BESLUTTER),
        )

    fun opprettVeilederToken(navIdent: String = "V123456"): Jwt =
        opprettGyldigToken(
            navIdent = navIdent,
            azureGrupper = listOf(AZURE_GROUP_ID_VEILEDER),
        )

    fun opprettSaksbehandlerOgBeslutterToken(navIdent: String = "AB12345"): Jwt =
        opprettGyldigToken(
            navIdent = navIdent,
            azureGrupper =
                listOf(
                    AZURE_GROUP_ID_SAKSBEHANDLER,
                    AZURE_GROUP_ID_BESLUTTER,
                ),
        )

    fun opprettTokenMedAlleRoller(navIdent: String = "BINKUSMAXIMUS123"): Jwt =
        opprettGyldigToken(
            navIdent = navIdent,
            azureGrupper =
                listOf(
                    AZURE_GROUP_ID_SAKSBEHANDLER,
                    AZURE_GROUP_ID_BESLUTTER,
                    AZURE_GROUP_ID_VEILEDER,
                ),
        )
}
