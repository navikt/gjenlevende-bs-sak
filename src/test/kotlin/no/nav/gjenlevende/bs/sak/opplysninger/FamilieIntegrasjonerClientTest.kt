package no.nav.gjenlevende.bs.sak.opplysninger

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FamilieIntegrasjonerClientTest {
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

    @BeforeEach
    fun setup() {
    }

    @AfterEach
    fun tearDownEachTest() {
        wireMockServer.resetAll()
    }

    @Test
    fun `saksbehandler har tilgang til personer med relasjoner`() {
        wireMockServer.stubFor(
            get(anyUrl())
                .willReturn(
                    aResponse()
                        .withBody(tilgangTilPersonerMedRelasjonerJson),
                ),
        )
    }

    private val tilgangTilPersonerMedRelasjonerJson =
        """
        {
            "personIdent": "01010199999",
            "harTilgang": true,
        }
        """.trimIndent()
}
