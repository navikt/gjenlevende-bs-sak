package no.nav.gjenlevende.bs.sak.felles.sikkerhet

import no.nav.gjenlevende.bs.sak.security.JwtTestHelper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class SikkerhetContextTest {
    @AfterEach
    fun ryddOpp() {
        SecurityContextHolder.clearContext()
    }

    private fun settOppSecurityContext(jwt: Jwt) {
        val authentication = JwtAuthenticationToken(jwt)
        SecurityContextHolder.getContext().authentication = authentication
    }

    @Test
    fun `hentTokenInfo skal returnere BrukerToken for gyldig brukertoken`() {
        val jwt = JwtTestHelper.opprettSaksbehandlerToken(navIdent = "Z123456")
        settOppSecurityContext(jwt)

        val tokenInfo = SikkerhetContext.hentTokenInfo()

        assertTrue(tokenInfo is TokenInfo.BrukerToken)
        val brukerToken = tokenInfo as TokenInfo.BrukerToken

        assertEquals("Z123456", brukerToken.navIdent)
        assertEquals(jwt.tokenValue, brukerToken.tokenVerdi)
    }

    @Test
    fun `hentTokenInfo skal returnere ApplikasjonToken for M2M token`() {
        val jwt =
            JwtTestHelper.opprettMaskinTilMaskinToken(
                applikasjonNavn = "dev-gcp:team-gjenlevende:test-app",
                applikasjonId = "test-client-id",
            )
        settOppSecurityContext(jwt)

        val tokenInfo = SikkerhetContext.hentTokenInfo()

        assertTrue(tokenInfo is TokenInfo.ApplikasjonToken)
        val appToken = tokenInfo as TokenInfo.ApplikasjonToken

        assertEquals("dev-gcp:team-gjenlevende:test-app", appToken.applikasjonNavn)
        assertEquals("test-client-id", appToken.applikasjonId)
        assertEquals(jwt.tokenValue, appToken.tokenVerdi)
    }

    @Test
    fun `hentTokenInfo skal kaste exception når authentication mangler`() {
        val exception = assertThrows<IllegalStateException> { SikkerhetContext.hentTokenInfo() }

        assertEquals("Ingen authentication i SecurityContext", exception.message)
    }

    @Test
    fun `hentTokenInfo skal kaste exception når NAVident mangler i brukertoken`() {
        val jwt = JwtTestHelper.opprettTokenUtenNavIdent()
        settOppSecurityContext(jwt)

        val exception = assertThrows<IllegalStateException> { SikkerhetContext.hentTokenInfo() }

        assertEquals("Brukertoken mangler NAVident claim", exception.message)
    }

    @Test
    fun `erMaskinTilMaskinToken skal returnere true for M2M token`() {
        val jwt = JwtTestHelper.opprettMaskinTilMaskinToken()

        settOppSecurityContext(jwt)

        assertTrue(SikkerhetContext.erMaskinTilMaskinToken())
    }

    @Test
    fun `erMaskinTilMaskinToken skal returnere false for brukertoken`() {
        val jwt = JwtTestHelper.opprettSaksbehandlerToken()

        settOppSecurityContext(jwt)

        assertFalse(SikkerhetContext.erMaskinTilMaskinToken())
    }

    @Test
    fun `hentSaksbehandler skal returnere NAVident for brukertoken`() {
        val jwt = JwtTestHelper.opprettSaksbehandlerToken(navIdent = "A999888")

        settOppSecurityContext(jwt)

        val navIdent = SikkerhetContext.hentSaksbehandler()

        assertEquals("A999888", navIdent)
    }

    @Test
    fun `hentSaksbehandler skal kaste exception for M2M token`() {
        val jwt =
            JwtTestHelper.opprettMaskinTilMaskinToken(
                applikasjonNavn = "dev-gcp:team:min-app",
            )

        settOppSecurityContext(jwt)

        val exception = assertThrows<IllegalStateException> { SikkerhetContext.hentSaksbehandler() }

        assertTrue(exception.message!!.contains("Forventet brukertoken"))
        assertTrue(exception.message!!.contains("dev-gcp:team:min-app"))
    }

    @Test
    fun `hentSaksbehandlerEllerSystembruker skal returnere NAVident for brukertoken`() {
        val jwt = JwtTestHelper.opprettSaksbehandlerToken(navIdent = "B777666")

        settOppSecurityContext(jwt)

        val resultat = SikkerhetContext.hentSaksbehandlerEllerSystembruker()

        assertEquals("B777666", resultat)
    }

    @Test
    fun `hentSaksbehandlerEllerSystembruker skal returnere VL for M2M token`() {
        val jwt = JwtTestHelper.opprettMaskinTilMaskinToken()

        settOppSecurityContext(jwt)

        val resultat = SikkerhetContext.hentSaksbehandlerEllerSystembruker()

        assertEquals("VL", resultat)
    }

    @Test
    fun `hentSaksbehandlerEllerSystembruker skal returnere VL når ingen authentication`() {
        val resultat = SikkerhetContext.hentSaksbehandlerEllerSystembruker()

        assertEquals("VL", resultat)
    }

    @Test
    fun `hentSaksbehandlerEllerSystembruker skal kaste exception for brukertoken uten NAVident`() {
        val jwt = JwtTestHelper.opprettTokenUtenNavIdent()
        settOppSecurityContext(jwt)

        val exception =
            assertThrows<IllegalStateException> {
                SikkerhetContext.hentSaksbehandlerEllerSystembruker()
            }

        assertEquals("Brukertoken mangler NAVident claim", exception.message)
    }

    @Test
    fun `hentBrukerToken skal returnere token for brukertoken`() {
        val jwt = JwtTestHelper.opprettSaksbehandlerToken()
        settOppSecurityContext(jwt)

        val token = SikkerhetContext.hentBrukerToken()

        assertEquals(jwt.tokenValue, token)
    }

    @Test
    fun `hentBrukerToken skal kaste exception for M2M token`() {
        val jwt =
            JwtTestHelper.opprettMaskinTilMaskinToken(
                applikasjonNavn = "dev-gcp:team:annen-app",
            )
        settOppSecurityContext(jwt)

        val exception =
            assertThrows<IllegalStateException> {
                SikkerhetContext.hentBrukerToken()
            }

        assertTrue(exception.message!!.contains("Forventet brukertoken"))
        assertTrue(exception.message!!.contains("dev-gcp:team:annen-app"))
    }
}
