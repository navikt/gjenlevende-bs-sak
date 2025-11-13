package no.nav.gjenlevende.bs.sak.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class AzureJwtAuthenticationConverterTest {
    private val converter = AzureJwtAuthenticationConverter()

    @Test
    fun `skal konvertere gyldig token med saksbehandler rolle`() {
        val jwt = JwtTestHelper.opprettSaksbehandlerToken(navIdent = "A123456")

        val resultat = converter.convert(jwt)

        assertNotNull(resultat)
        assertTrue(resultat is JwtAuthenticationToken)

        val authToken = resultat as JwtAuthenticationToken
        assertEquals(jwt, authToken.token)

        val authorities = authToken.authorities.map { it.authority }

        assertEquals(1, authorities.size)
        assertTrue(authorities.contains("ROLE_SAKSBEHANDLER"))
    }

    @Test
    fun `skal konvertere gyldig token med beslutter rolle`() {
        val jwt = JwtTestHelper.opprettBeslutterToken(navIdent = "B123456")

        val resultat = converter.convert(jwt)

        assertNotNull(resultat)
        val authToken = resultat as JwtAuthenticationToken

        val authorities = authToken.authorities.map { it.authority }

        assertEquals(1, authorities.size)
        assertTrue(authorities.contains("ROLE_BESLUTTER"))
    }

    @Test
    fun `skal konvertere gyldig token med veileder rolle`() {
        val jwt = JwtTestHelper.opprettVeilederToken(navIdent = "V123456")

        val resultat = converter.convert(jwt)

        assertNotNull(resultat)
        val authToken = resultat as JwtAuthenticationToken

        val authorities = authToken.authorities.map { it.authority }

        assertEquals(1, authorities.size)
        assertTrue(authorities.contains("ROLE_VEILEDER"))
    }

    @Test
    fun `skal konvertere token med flere roller`() {
        val jwt = JwtTestHelper.opprettSaksbehandlerOgBeslutterToken(navIdent = "AB12345")

        val resultat = converter.convert(jwt)

        assertNotNull(resultat)
        val authToken = resultat as JwtAuthenticationToken

        val authorities = authToken.authorities.map { it.authority }

        assertEquals(2, authorities.size)
        assertTrue(authorities.contains("ROLE_SAKSBEHANDLER"))
        assertTrue(authorities.contains("ROLE_BESLUTTER"))
    }

    @Test
    fun `skal konvertere token med alle nåværende BS roller`() {
        val jwt = JwtTestHelper.opprettTokenMedAlleRoller(navIdent = "ADMIN123")

        val resultat = converter.convert(jwt)

        assertNotNull(resultat)
        val authToken = resultat as JwtAuthenticationToken

        val authorities = authToken.authorities.map { it.authority }

        assertEquals(3, authorities.size)
        assertTrue(authorities.contains("ROLE_SAKSBEHANDLER"))
        assertTrue(authorities.contains("ROLE_BESLUTTER"))
        assertTrue(authorities.contains("ROLE_VEILEDER"))
    }

    @Test
    fun `skal kaste exception når NAVident claim mangler`() {
        val jwt = JwtTestHelper.opprettTokenUtenNavIdent()

        val exception = assertThrows<ManglendeClaimException> { converter.convert(jwt) }

        assertEquals("Token mangler påkrevd claim: NAVident", exception.message)
    }

    @Test
    fun `skal kaste tilgang exception når groups claim mangler`() {
        val jwt = JwtTestHelper.opprettTokenUtenGrupper()

        val exception = assertThrows<UtilstrekkeligTilgangException> { converter.convert(jwt) }

        assertEquals(
            "Bruker har ikke tilgang til applikasjonen. Mangler påkrevde gruppemedlemskap.",
            exception.message,
        )
    }

    @Test
    fun `skal kaste tilgang exception når bruker ikke har gyldige Azure grupper`() {
        val jwt = JwtTestHelper.opprettTokenMedUgyldigeGrupper()

        val exception = assertThrows<UtilstrekkeligTilgangException> { converter.convert(jwt) }

        assertEquals(
            "Bruker har ikke tilgang til applikasjonen. Mangler påkrevde gruppemedlemskap.",
            exception.message,
        )
    }

    @Test
    fun `skal bevare JWT token i resulterende authentication token`() {
        val jwt =
            JwtTestHelper.opprettGyldigToken(
                navIdent = "TEST123",
                navn = "Test Bruker",
                epost = "test.bruker@nav.no",
            )

        val resultat = converter.convert(jwt) as JwtAuthenticationToken

        assertEquals(jwt, resultat.token)
        assertEquals("TEST123", resultat.token.getClaimAsString("NAVident"))
        assertEquals("Test Bruker", resultat.token.getClaimAsString("name"))
        assertEquals("test.bruker@nav.no", resultat.token.getClaimAsString("preferred_username"))
    }

    @Test
    fun `skal håndtere token med både gyldige og ugyldige Azure grupper`() {
        val jwt =
            JwtTestHelper.opprettGyldigToken(
                navIdent = "MIKSED123",
                azureGrupper =
                    listOf(
                        "5357fbfa-de25-4d23-86a6-f67caf8ddd63", // SAKSBEHANDLER
                        "00000000-0000-0000-0000-000000000000", // Ukjent gruppe
                        "fda781b0-b82c-4049-919d-3b05623f05fb", // BESLUTTER
                    ),
            )

        val resultat = converter.convert(jwt) as JwtAuthenticationToken

        val authorities = resultat.authorities.map { it.authority }

        assertEquals(2, authorities.size)
        assertTrue(authorities.contains("ROLE_SAKSBEHANDLER"))
        assertTrue(authorities.contains("ROLE_BESLUTTER"))
    }
}
