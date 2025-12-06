package no.nav.gjenlevende.bs.sak.opplysninger

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.restclient.RestTemplateBuilder
import org.springframework.web.client.RestOperations
import java.net.URI

class FamilieIntegrasjonerClientTest {
    companion object {
        private lateinit var wireMockServer: WireMockServer
        private val restOperations: RestOperations = RestTemplateBuilder().build()
        lateinit var familieIntegrasjonerClient: FamilieIntegrasjonerClient

        @BeforeAll
        @JvmStatic
        fun initClass() {
            wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())
            wireMockServer.start()

            familieIntegrasjonerClient =
                FamilieIntegrasjonerClient(
                    URI.create(wireMockServer.baseUrl()),
                    restOperations,
                )
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            wireMockServer.stop()
        }
    }

    @BeforeEach
    fun configure() {
        WireMock.configureFor(wireMockServer.port())
    }

    @AfterEach
    fun tearDownEachTest() {
        wireMockServer.resetAll()
    }

    @Test
    fun `saksbehandler har tilgang til personer med relasjoner`() {
        wireMockServer.stubFor(
            post(anyUrl())
                .willReturn(
                    aResponse()
                        .withBody(tilgangTilPersonerMedRelasjonerJson)
                        .withHeader("Content-Type", "application/json"),
                ),
        )

        val response = familieIntegrasjonerClient.sjekkTilgangTilPersonMedRelasjoner("01010199999")
        Assertions.assertThat(response).isNotNull
        Assertions.assertThat(response.harTilgang).isTrue
    }

    private val tilgangTilPersonerMedRelasjonerJson =
        """
        {
            "personIdent": "01010199999",
            "harTilgang": true
        }
        """.trimIndent()
}
