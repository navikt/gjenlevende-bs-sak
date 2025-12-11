package no.nav.gjenlevende.bs.sak.tilgangskontroll

import io.mockk.every
import io.mockk.mockk
import no.nav.gjenlevende.bs.sak.security.JwtTestHelper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class TilgangskontrollControllerTest {
    private lateinit var tilgangsmaskinClient: TilgangsmaskinClient
    private lateinit var controller: TilgangskontrollController

    @BeforeEach
    fun setUp() {
        tilgangsmaskinClient = mockk()
        controller = TilgangskontrollController(tilgangsmaskinClient)

        val jwt = JwtTestHelper.opprettSaksbehandlerToken("Z990227")
        val authentication = JwtAuthenticationToken(jwt)
        SecurityContextHolder.getContext().authentication = authentication
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `harTilgang returnerer riktig respons med tilgang`() {
        val personident = "12345678901"
        every { tilgangsmaskinClient.sjekkTilgang(personident) } returns TilgangResultat.godkjent()

        val response = controller.harTilgang(TilgangRequest(personident))

        assertEquals("Z990227", response.ansattId)
        assertEquals(personident, response.personident)
        assertTrue(response.harTilgang)
    }

    @Test
    fun `harTilgang returnerer riktig respons uten tilgang`() {
        val personident = "16449348706"
        every { tilgangsmaskinClient.sjekkTilgang(personident) } returns
            TilgangResultat.avvist(
                Avvisningsgrunn.AVVIST_HABILITET,
                "Du har ikke tilgang til data om deg selv eller dine nærstående",
            )

        val response = controller.harTilgang(TilgangRequest(personident))

        assertEquals("Z990227", response.ansattId)
        assertEquals(personident, response.personident)
        assertFalse(response.harTilgang)
        assertEquals("AVVIST_HABILITET", response.avvisningsgrunn)
        assertEquals("Du har ikke tilgang til data om deg selv eller dine nærstående", response.begrunnelse)
    }

    @Test
    fun `harTilgangBulk returnerer riktige antall og filtrert liste`() {
        val personidenter = listOf("12345678901", "16449348706", "99999999999")
        val personidenterMedTilgang = listOf("12345678901", "99999999999")

        every { tilgangsmaskinClient.harTilgangTilBrukere(personidenter) } returns personidenterMedTilgang

        val response = controller.harTilgangBulk(TilgangBulkRequest(personidenter))

        assertEquals("Z990227", response.ansattId)
        assertEquals(3, response.totalAntall)
        assertEquals(2, response.antallMedTilgang)
        assertEquals(personidenterMedTilgang, response.personidenterMedTilgang)
    }

    @Test
    fun `harTilgangBulk returnerer tom liste når ingen har tilgang`() {
        val personidenter = listOf("16449348706", "28422453875")

        every { tilgangsmaskinClient.harTilgangTilBrukere(personidenter) } returns emptyList()

        val response = controller.harTilgangBulk(TilgangBulkRequest(personidenter))

        assertEquals("Z990227", response.ansattId)
        assertEquals(2, response.totalAntall)
        assertEquals(0, response.antallMedTilgang)
        assertTrue(response.personidenterMedTilgang.isEmpty())
    }

    @Test
    fun `harTilgang kaster exception når NAVident mangler i token`() {
        val jwt = JwtTestHelper.opprettTokenUtenNavIdent()
        val authentication = JwtAuthenticationToken(jwt)
        SecurityContextHolder.getContext().authentication = authentication

        assertThrows<IllegalStateException> {
            controller.harTilgang(TilgangRequest("12345678901"))
        }
    }

    @Test
    fun `harTilgang kaster exception når ingen JWT authentication finnes`() {
        SecurityContextHolder.clearContext()

        assertThrows<IllegalStateException> {
            controller.harTilgang(TilgangRequest("12345678901"))
        }
    }
}
