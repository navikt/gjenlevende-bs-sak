package no.nav.gjenlevende.bs.sak.tilgangskontroll

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.mockk.every
import io.mockk.mockk
import no.nav.gjenlevende.bs.sak.security.JwtTestHelper
import no.nav.gjenlevende.bs.sak.texas.TexasClient
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class TilgangsmaskinClientTest {
    companion object {
        private lateinit var wireMockServer: WireMockServer

        @BeforeAll
        @JvmStatic
        fun initClass() {
            wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())
            wireMockServer.start()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            wireMockServer.stop()
        }
    }

    private lateinit var texasClient: TexasClient
    private lateinit var tilgangsmaskinClient: TilgangsmaskinClient

    @BeforeEach
    fun setUp() {
        texasClient = mockk()
        every { texasClient.hentOboToken(any(), any()) } returns "test-obo-token"

        tilgangsmaskinClient =
            TilgangsmaskinClient(
                tilgangsmaskinUrl = "http://localhost:${wireMockServer.port()}",
                tilgangsmaskinScope = "api://test/.default",
                texasClient = texasClient,
            )

        val jwt = JwtTestHelper.opprettSaksbehandlerToken("Z990227")
        val authentication = JwtAuthenticationToken(jwt)
        SecurityContextHolder.getContext().authentication = authentication
    }

    @AfterEach
    fun tearDownEachTest() {
        wireMockServer.resetAll()
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `harTilgangTilBruker returnerer true når API returnerer status 204`() {
        val personident = "12345678901"

        wireMockServer.stubFor(
            post(urlEqualTo("/api/v1/bulk/obo/KJERNE_REGELTYPE"))
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.MULTI_STATUS.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            """
                            {
                                "ansattId": "Z990227",
                                "resultater": [
                                    {
                                        "brukerId": "$personident",
                                        "status": 204
                                    }
                                ]
                            }
                            """.trimIndent(),
                        ),
                ),
        )

        val resultat = tilgangsmaskinClient.harTilgangTilBruker(personident)

        assertTrue(resultat)
    }

    @Test
    fun `harTilgangTilBruker returnerer true når API returnerer status 404 - bruker finnes ikke i PDL`() {
        val personident = "99999999999"

        wireMockServer.stubFor(
            post(urlEqualTo("/api/v1/bulk/obo/KJERNE_REGELTYPE"))
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.MULTI_STATUS.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            """
                            {
                                "ansattId": "Z990227",
                                "resultater": [
                                    {
                                        "brukerId": "$personident",
                                        "status": 404
                                    }
                                ]
                            }
                            """.trimIndent(),
                        ),
                ),
        )

        val resultat = tilgangsmaskinClient.harTilgangTilBruker(personident)

        assertTrue(resultat)
    }

    @Test
    fun `harTilgangTilBruker returnerer false når API returnerer status 403 - habilitet`() {
        val personident = "16449348706"

        wireMockServer.stubFor(
            post(urlEqualTo("/api/v1/bulk/obo/KJERNE_REGELTYPE"))
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.MULTI_STATUS.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            """
                            {
                                "ansattId": "Z990227",
                                "resultater": [
                                    {
                                        "brukerId": "$personident",
                                        "status": 403,
                                        "detaljer": {
                                            "avvisningsgrunn": "AVVIST_HABILITET",
                                            "begrunnelse": "Du har ikke tilgang til data om deg selv eller dine nærstående"
                                        }
                                    }
                                ]
                            }
                            """.trimIndent(),
                        ),
                ),
        )

        val resultat = tilgangsmaskinClient.harTilgangTilBruker(personident)

        assertFalse(resultat)
    }

    @Test
    fun `harTilgangTilBruker returnerer false når ingen resultat funnet for personident`() {
        val personident = "12345678901"

        wireMockServer.stubFor(
            post(urlEqualTo("/api/v1/bulk/obo/KJERNE_REGELTYPE"))
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.MULTI_STATUS.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            """
                            {
                                "ansattId": "Z990227",
                                "resultater": []
                            }
                            """.trimIndent(),
                        ),
                ),
        )

        val resultat = tilgangsmaskinClient.harTilgangTilBruker(personident)

        assertFalse(resultat)
    }

    @Test
    fun `harTilgangTilBruker returnerer false ved WebClient feil`() {
        val personident = "12345678901"

        wireMockServer.stubFor(
            post(urlEqualTo("/api/v1/bulk/obo/KJERNE_REGELTYPE"))
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                ),
        )

        val resultat = tilgangsmaskinClient.harTilgangTilBruker(personident)

        assertFalse(resultat)
    }

    @Test
    fun `harTilgangTilBrukere returnerer tom liste naar input er tom`() {
        val resultat = tilgangsmaskinClient.harTilgangTilBrukere(emptyList())

        assertTrue(resultat.isEmpty())
    }

    @Test
    fun `harTilgangTilBrukere kaster exception naar mer enn 1000 personidenter`() {
        val personidenter = (1..1001).map { "1234567890$it" }

        assertThrows<IllegalArgumentException> {
            tilgangsmaskinClient.harTilgangTilBrukere(personidenter)
        }
    }

    @Test
    fun `harTilgangTilBrukere filtrerer og returnerer kun personidenter med tilgang`() {
        val personidentMedTilgang = "12345678901"
        val personidentUtenTilgang = "16449348706"
        val personidentIkkeFunnet = "99999999999"

        wireMockServer.stubFor(
            post(urlEqualTo("/api/v1/bulk/obo/KJERNE_REGELTYPE"))
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.MULTI_STATUS.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                            """
                            {
                                "ansattId": "Z990227",
                                "resultater": [
                                    {
                                        "brukerId": "$personidentMedTilgang",
                                        "status": 204
                                    },
                                    {
                                        "brukerId": "$personidentUtenTilgang",
                                        "status": 403
                                    },
                                    {
                                        "brukerId": "$personidentIkkeFunnet",
                                        "status": 404
                                    }
                                ]
                            }
                            """.trimIndent(),
                        ),
                ),
        )

        val resultat =
            tilgangsmaskinClient.harTilgangTilBrukere(
                listOf(personidentMedTilgang, personidentUtenTilgang, personidentIkkeFunnet),
            )

        assertEquals(2, resultat.size)
        assertTrue(resultat.contains(personidentMedTilgang))
        assertTrue(resultat.contains(personidentIkkeFunnet))
        assertFalse(resultat.contains(personidentUtenTilgang))
    }
}
