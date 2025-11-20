package no.nav.gjenlevende.bs.sak.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AzureJwtAuthenticationConverterTest {
    private val converter = AzureJwtAuthenticationConverter()

    @Test
    fun `skal konvertere gyldig token med saksbehandler rolle`() {
        val jwt = JwtTestHelper.opprettSaksbehandlerToken(navIdent = "A123456")

        val resultat = converter.convert(jwt)

        assertNotNull(resultat)
        assertTrue(resultat is GjenlevendeJwtAuthenticationToken)

        val authToken = resultat as GjenlevendeJwtAuthenticationToken

        val principal = authToken.principal
        assertTrue(principal is TokenPrincipal.Bruker)
        assertEquals("A123456", (principal as TokenPrincipal.Bruker).navIdent)

        val authorities = authToken.authorities.map { it.authority }

        assertEquals(1, authorities.size)
        assertTrue(authorities.contains("ROLE_SAKSBEHANDLER"))
    }

    @Test
    fun `skal konvertere gyldig token med beslutter rolle`() {
        val jwt = JwtTestHelper.opprettBeslutterToken(navIdent = "B123456")

        val resultat = converter.convert(jwt)

        assertNotNull(resultat)
        val authToken = resultat as GjenlevendeJwtAuthenticationToken

        val principal = authToken.principal
        assertTrue(principal is TokenPrincipal.Bruker)
        assertEquals("B123456", (principal as TokenPrincipal.Bruker).navIdent)

        val authorities = authToken.authorities.map { it.authority }

        assertEquals(1, authorities.size)
        assertTrue(authorities.contains("ROLE_BESLUTTER"))
    }

    @Test
    fun `skal konvertere gyldig token med veileder rolle`() {
        val jwt = JwtTestHelper.opprettVeilederToken(navIdent = "V123456")

        val resultat = converter.convert(jwt)

        assertNotNull(resultat)
        val authToken = resultat as GjenlevendeJwtAuthenticationToken

        val principal = authToken.principal
        assertTrue(principal is TokenPrincipal.Bruker)
        assertEquals("V123456", (principal as TokenPrincipal.Bruker).navIdent)

        val authorities = authToken.authorities.map { it.authority }

        assertEquals(1, authorities.size)
        assertTrue(authorities.contains("ROLE_VEILEDER"))
    }

    @Test
    fun `skal konvertere token med flere roller`() {
        val jwt = JwtTestHelper.opprettSaksbehandlerOgBeslutterToken(navIdent = "AB12345")

        val resultat = converter.convert(jwt)

        assertNotNull(resultat)
        val authToken = resultat as GjenlevendeJwtAuthenticationToken

        val principal = authToken.principal
        assertTrue(principal is TokenPrincipal.Bruker)
        assertEquals("AB12345", (principal as TokenPrincipal.Bruker).navIdent)

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
        val authToken = resultat as GjenlevendeJwtAuthenticationToken

        val principal = authToken.principal
        assertTrue(principal is TokenPrincipal.Bruker)
        assertEquals("ADMIN123", (principal as TokenPrincipal.Bruker).navIdent)

        val authorities = authToken.authorities.map { it.authority }

        assertEquals(3, authorities.size)
        assertTrue(authorities.contains("ROLE_SAKSBEHANDLER"))
        assertTrue(authorities.contains("ROLE_BESLUTTER"))
        assertTrue(authorities.contains("ROLE_VEILEDER"))
    }

    @Test
    fun `skal kaste exception når både NAVident og azp_name mangler`() {
        val jwt = JwtTestHelper.opprettTokenUtenNavIdent()

        val exception = assertThrows<ManglendeClaimException> { converter.convert(jwt) }

        assertTrue(exception.message!!.contains("NAVident"))
        assertTrue(exception.message!!.contains("azp_name"))
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
    fun `skal bevare JWT token og ekstrahere brukerinfo korrekt`() {
        val jwt =
            JwtTestHelper.opprettGyldigToken(
                navIdent = "TEST123",
                navn = "Test Bruker",
                epost = "test.bruker@nav.no",
            )

        val resultat = converter.convert(jwt) as GjenlevendeJwtAuthenticationToken

        val principal = resultat.principal
        assertTrue(principal is TokenPrincipal.Bruker)
        val bruker = principal as TokenPrincipal.Bruker

        assertEquals("TEST123", bruker.navIdent)
        assertEquals("Test Bruker", bruker.navn)
        assertEquals("test.bruker@nav.no", bruker.epost)
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

        val resultat = converter.convert(jwt) as GjenlevendeJwtAuthenticationToken

        val authorities = resultat.authorities.map { it.authority }

        assertEquals(2, authorities.size)
        assertTrue(authorities.contains("ROLE_SAKSBEHANDLER"))
        assertTrue(authorities.contains("ROLE_BESLUTTER"))
    }

    // M2M Token Tests

    @Test
    fun `skal konvertere gyldig M2M token`() {
        val jwt =
            JwtTestHelper.opprettM2MToken(
                azpNavn = "gjenlevende-bs-frontend",
                clientId = "test-client-123",
            )

        val resultat = converter.convert(jwt)

        assertNotNull(resultat)
        assertTrue(resultat is GjenlevendeJwtAuthenticationToken)

        val authToken = resultat as GjenlevendeJwtAuthenticationToken

        val principal = authToken.principal
        assertTrue(principal is TokenPrincipal.Applikasjon)
        val app = principal as TokenPrincipal.Applikasjon

        assertEquals("gjenlevende-bs-frontend", app.azpNavn)
        assertEquals("test-client-123", app.clientId)

        val authorities = authToken.authorities.map { it.authority }
        assertEquals(1, authorities.size)
        assertTrue(authorities.contains("ROLE_APPLICATION"))
    }

    @Test
    fun `skal kaste exception når M2M token mangler azp claim`() {
        val jwt = JwtTestHelper.opprettM2MTokenUtenAzp()

        val exception = assertThrows<ManglendeClaimException> { converter.convert(jwt) }

        assertTrue(exception.message!!.contains("azp"))
    }

    @Test
    fun `getName skal returnere azpNavn for M2M token`() {
        val jwt = JwtTestHelper.opprettM2MToken(azpNavn = "test-app")

        val resultat = converter.convert(jwt) as GjenlevendeJwtAuthenticationToken

        assertEquals("test-app", resultat.name)
    }

    @Test
    fun `getName skal returnere navIdent for brukertoken`() {
        val jwt = JwtTestHelper.opprettSaksbehandlerToken(navIdent = "TEST123")

        val resultat = converter.convert(jwt) as GjenlevendeJwtAuthenticationToken

        assertEquals("TEST123", resultat.name)
    }
}
