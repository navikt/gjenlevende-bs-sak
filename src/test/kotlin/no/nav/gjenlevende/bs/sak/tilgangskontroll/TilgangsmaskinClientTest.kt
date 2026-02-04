package no.nav.gjenlevende.bs.sak.tilgangskontroll

import io.mockk.every
import io.mockk.mockk
import no.nav.gjenlevende.bs.sak.texas.TexasClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.net.URI
import java.nio.charset.StandardCharsets

class TilgangsmaskinClientTest {
    private val tilgangsmaskinUrl = URI.create("http://localhost:8080")
    private val tilgangsmaskinScope = "api://test/.default"

    private val texasClient = mockk<TexasClient>()

    private lateinit var client: TilgangsmaskinClient
    private val tilgangsmaskinWebClient = mockk<WebClient>(relaxed = true)

    @BeforeEach
    fun setup() {
        client =
            TilgangsmaskinClient(
                tilgangsmaskinUrl = tilgangsmaskinUrl,
                tilgangsmaskinScope = tilgangsmaskinScope,
                texasClient = texasClient,
                tilgangsmaskinWebClient = tilgangsmaskinWebClient,
            )
        every { texasClient.hentOboToken(tilgangsmaskinScope) } returns "gyldig-token"
    }

    @Nested
    inner class SjekkTilgangEnkel {
        @Test
        fun `saksbehandler har tilgang til vanlig bruker - returnerer 204`() {
            val navIdent = "Z123456"
            val personident = "12345678901"

            mockkWebclient(
                expectedResponse =
                    EnkelTilgangsResponse(
                        harTilgang = true,
                        navIdent = navIdent,
                        personident = personident,
                        avvisningskode = null,
                        begrunnelse = null,
                    ),
            )

            val resultat = client.sjekkTilgangEnkel(navIdent, personident)

            assertTrue(resultat.harTilgang)
            assertEquals(navIdent, resultat.navIdent)
            assertEquals(personident, resultat.personident)
            assertNull(resultat.avvisningskode)
            assertNull(resultat.begrunnelse)
        }

        @Test
        fun `saksbehandler søker opp seg selv - avvises med AVVIST_HABILITET`() {
            val navIdent = "Z123456"
            val personident = "12345678901"

            val forbiddenBody =
                """
                {
                    "type": "about:blank",
                    "title": "AVVIST_HABILITET",
                    "status": 403,
                    "instance": "/dev/komplett/$navIdent/$personident",
                    "brukerIdent": "$personident",
                    "navIdent": "$navIdent",
                    "begrunnelse": "Du har ikke tilgang til data om deg selv eller dine nærstående",
                    "traceId": "abc123",
                    "kanOverstyres": false
                }
                """.trimIndent()

            every {
                tilgangsmaskinWebClient
                    .get()
            } throws
                HttpClientErrorException.create(
                    HttpStatus.FORBIDDEN,
                    "Forbidden",
                    org.springframework.http.HttpHeaders(),
                    forbiddenBody.toByteArray(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8,
                )

            val resultat = client.sjekkTilgangEnkel(navIdent, personident)

            assertFalse(resultat.harTilgang)
            assertEquals(navIdent, resultat.navIdent)
            assertEquals(personident, resultat.personident)
            assertEquals(Avvisningskode.AVVIST_HABILITET, resultat.avvisningskode)
            assertEquals("Du har ikke tilgang til data om deg selv eller dine nærstående", resultat.begrunnelse)
        }

        @Test
        fun `saksbehandler søker opp eget barn - avvises med AVVIST_HABILITET`() {
            val navIdent = "Z123456"
            val personident = "12345678902"

            val forbiddenBody =
                """
                {
                    "type": "about:blank",
                    "title": "AVVIST_HABILITET",
                    "status": 403,
                    "instance": "/dev/komplett/$navIdent/$personident",
                    "brukerIdent": "$personident",
                    "navIdent": "$navIdent",
                    "begrunnelse": "Du har ikke tilgang til data om deg selv eller dine nærstående",
                    "traceId": "abc123",
                    "kanOverstyres": false
                }
                """.trimIndent()

            every {
                tilgangsmaskinWebClient
                    .get()
            } throws
                HttpClientErrorException.create(
                    HttpStatus.FORBIDDEN,
                    "Forbidden",
                    org.springframework.http.HttpHeaders(),
                    forbiddenBody.toByteArray(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8,
                )

            val resultat = client.sjekkTilgangEnkel(navIdent, personident)

            assertFalse(resultat.harTilgang)
            assertEquals(Avvisningskode.AVVIST_HABILITET, resultat.avvisningskode)
            assertEquals("Du har ikke tilgang til data om deg selv eller dine nærstående", resultat.begrunnelse)
        }

        @Test
        fun `saksbehandler søker opp partner - avvises med AVVIST_HABILITET`() {
            val navIdent = "Z123456"
            val personident = "12345678903"

            val forbiddenBody =
                """
                {
                    "type": "about:blank",
                    "title": "AVVIST_HABILITET",
                    "status": 403,
                    "instance": "/dev/komplett/$navIdent/$personident",
                    "brukerIdent": "$personident",
                    "navIdent": "$navIdent",
                    "begrunnelse": "Du har ikke tilgang til data om deg selv eller dine nærstående",
                    "traceId": "abc123",
                    "kanOverstyres": false
                }
                """.trimIndent()
            every {
                tilgangsmaskinWebClient
                    .get()
            } throws
                HttpClientErrorException.create(
                    HttpStatus.FORBIDDEN,
                    "Forbidden",
                    org.springframework.http.HttpHeaders(),
                    forbiddenBody.toByteArray(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8,
                )

            val resultat = client.sjekkTilgangEnkel(navIdent, personident)

            assertFalse(resultat.harTilgang)
            assertEquals(Avvisningskode.AVVIST_HABILITET, resultat.avvisningskode)
        }

        @Test
        fun `saksbehandler uten riktig gruppe avvises fra strengt fortrolig bruker`() {
            val navIdent = "Z654321"
            val personident = "12345678904"

            val forbiddenBody =
                """
                {
                    "type": "about:blank",
                    "title": "AVVIST_STRENGT_FORTROLIG_ADRESSE",
                    "status": 403,
                    "instance": "/dev/komplett/$navIdent/$personident",
                    "brukerIdent": "$personident",
                    "navIdent": "$navIdent",
                    "begrunnelse": "Du har ikke tilgang til brukere med strengt fortrolig adresse",
                    "traceId": "abc123",
                    "kanOverstyres": false
                }
                """.trimIndent()

            every {
                tilgangsmaskinWebClient
                    .get()
            } throws
                HttpClientErrorException.create(
                    HttpStatus.FORBIDDEN,
                    "Forbidden",
                    org.springframework.http.HttpHeaders(),
                    forbiddenBody.toByteArray(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8,
                )

            val resultat = client.sjekkTilgangEnkel(navIdent, personident)

            assertFalse(resultat.harTilgang)
            assertEquals(Avvisningskode.AVVIST_STRENGT_FORTROLIG_ADRESSE, resultat.avvisningskode)
            assertEquals("Du har ikke tilgang til brukere med strengt fortrolig adresse", resultat.begrunnelse)
        }

        @Test
        fun `saksbehandler uten riktig gruppe avvises fra fortrolig bruker`() {
            val navIdent = "Z654321"
            val personident = "12345678905"

            val forbiddenBody =
                """
                {
                    "type": "about:blank",
                    "title": "AVVIST_FORTROLIG_ADRESSE",
                    "status": 403,
                    "instance": "/dev/komplett/$navIdent/$personident",
                    "brukerIdent": "$personident",
                    "navIdent": "$navIdent",
                    "begrunnelse": "Du har ikke tilgang til brukere med fortrolig adresse",
                    "traceId": "abc123",
                    "kanOverstyres": false
                }
                """.trimIndent()

            every {
                tilgangsmaskinWebClient.get()
            } throws
                HttpClientErrorException.create(
                    HttpStatus.FORBIDDEN,
                    "Forbidden",
                    org.springframework.http.HttpHeaders(),
                    forbiddenBody.toByteArray(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8,
                )

            val resultat = client.sjekkTilgangEnkel(navIdent, personident)

            assertFalse(resultat.harTilgang)
            assertEquals(Avvisningskode.AVVIST_FORTROLIG_ADRESSE, resultat.avvisningskode)
            assertEquals("Du har ikke tilgang til brukere med fortrolig adresse", resultat.begrunnelse)
        }

        @Test
        fun `saksbehandler avvises fra bruker i annet geografisk område`() {
            val navIdent = "Z654321"
            val personident = "12345678901"

            val forbiddenBody =
                """
                {
                    "type": "about:blank",
                    "title": "AVVIST_GEOGRAFISK",
                    "status": 403,
                    "instance": "/dev/komplett/$navIdent/$personident",
                    "brukerIdent": "$personident",
                    "navIdent": "$navIdent",
                    "begrunnelse": "Du har ikke tilgang til brukerens geografiske område eller oppfølgingsenhet",
                    "traceId": "abc123",
                    "kanOverstyres": false
                }
                """.trimIndent()

            every {
                tilgangsmaskinWebClient.get()
            } throws
                HttpClientErrorException.create(
                    HttpStatus.FORBIDDEN,
                    "Forbidden",
                    org.springframework.http.HttpHeaders(),
                    forbiddenBody.toByteArray(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8,
                )

            val resultat = client.sjekkTilgangEnkel(navIdent, personident)

            assertFalse(resultat.harTilgang)
            assertEquals(Avvisningskode.AVVIST_GEOGRAFISK, resultat.avvisningskode)
            assertEquals("Du har ikke tilgang til brukerens geografiske område eller oppfølgingsenhet", resultat.begrunnelse)
        }

        @Test
        fun `saksbehandler uten tilgang til egen ansatt avvises med AVVIST_SKJERMING`() {
            val navIdent = "Z123456"
            val personident = "12345678906"

            val forbiddenBody =
                """
                {
                    "type": "about:blank",
                    "title": "AVVIST_SKJERMING",
                    "status": 403,
                    "instance": "/dev/komplett/$navIdent/$personident",
                    "brukerIdent": "$personident",
                    "navIdent": "$navIdent",
                    "begrunnelse": "Du har ikke tilgang til Nav-ansatte og andre skjermede brukere",
                    "traceId": "abc123",
                    "kanOverstyres": false
                }
                """.trimIndent()

            every {
                tilgangsmaskinWebClient
                    .get()
            } throws
                HttpClientErrorException.create(
                    HttpStatus.FORBIDDEN,
                    "Forbidden",
                    org.springframework.http.HttpHeaders(),
                    forbiddenBody.toByteArray(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8,
                )

            val resultat = client.sjekkTilgangEnkel(navIdent, personident)

            assertFalse(resultat.harTilgang)
            assertEquals(Avvisningskode.AVVIST_SKJERMING, resultat.avvisningskode)
            assertEquals("Du har ikke tilgang til Nav-ansatte og andre skjermede brukere", resultat.begrunnelse)
        }

        @Test
        fun `saksbehandler uten tilgang søker opp annen Nav-ansatt - avvises med AVVIST_SKJERMING`() {
            val navIdent = "Z123456"
            val personident = "12345678907"

            val forbiddenBody =
                """
                {
                    "type": "about:blank",
                    "title": "AVVIST_SKJERMING",
                    "status": 403,
                    "instance": "/dev/komplett/$navIdent/$personident",
                    "brukerIdent": "$personident",
                    "navIdent": "$navIdent",
                    "begrunnelse": "Du har ikke tilgang til Nav-ansatte og andre skjermede brukere",
                    "traceId": "abc123",
                    "kanOverstyres": false
                }
                """.trimIndent()

            every {
                tilgangsmaskinWebClient.get()
            } throws
                HttpClientErrorException.create(
                    HttpStatus.FORBIDDEN,
                    "Forbidden",
                    org.springframework.http.HttpHeaders(),
                    forbiddenBody.toByteArray(StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8,
                )

            val resultat = client.sjekkTilgangEnkel(navIdent, personident)

            assertFalse(resultat.harTilgang)
            assertEquals(Avvisningskode.AVVIST_SKJERMING, resultat.avvisningskode)
            assertEquals("Du har ikke tilgang til Nav-ansatte og andre skjermede brukere", resultat.begrunnelse)
        }

        private fun mockkWebclient(
            expectedResponse: EnkelTilgangsResponse,
        ) {
            val responseSpec = mockk<WebClient.ResponseSpec>()
            val requestHeadersSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()

            every { tilgangsmaskinWebClient.get() } returns requestHeadersSpec
            every { requestHeadersSpec.uri(any<URI>()) } returns requestHeadersSpec
            every { requestHeadersSpec.headers(any()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono<EnkelTilgangsResponse>() } returns Mono.just(expectedResponse)
        }
    }

    @Nested
    inner class SjekkTilgangBulk {

        @Test
        fun `saksbehandler har ikke tilgang til brukere - returnerer ikke 204`() {
            val personidenter = listOf("12345678901", "12345678901")
            val expectedResponse = lagBulkRespons(personidenter, status = 403)
            mockkWebclientBulk(expectedResponse)

            val resultat = client.sjekkTilgangBulk(personidenter)
            assertTrue(resultat.resultater.none { it.harTilgang })
        }

        @Test
        fun `saksbehandler har tilgang til brukere - returnerer 204`() {
            val personidenter = listOf("12345678901", "12345678901")
            val expectedResponse = lagBulkRespons(personidenter, status = 204)
            mockkWebclientBulk(expectedResponse)

            val resultat = client.sjekkTilgangBulk(personidenter)
            assertTrue(resultat.resultater.all { it.harTilgang })
        }

        private fun lagBulkRespons(
            personident: List<String>,
            status: Int,
        ): BulkTilgangsResponse = BulkTilgangsResponse(
            navIdent = "Z123456",
            resultater =
                personident.map {  pid ->
                    TilgangsResultat(
                        personident = pid,
                        status = status,
                        detaljer = null,
                    )
                }.toSet(),
        )

        private fun mockkWebclientBulk(
            expectedResponse: BulkTilgangsResponse,
        ) {
            val responseSpec = mockk<WebClient.ResponseSpec>()
            val requestHeadersSpec = mockk<RequestBodyUriSpec>()

            every { tilgangsmaskinWebClient.post() } returns requestHeadersSpec
            every { requestHeadersSpec.uri(any<URI>()) } returns requestHeadersSpec
            every { requestHeadersSpec.bodyValue(any<List<String>>()) } returns requestHeadersSpec
            every { requestHeadersSpec.headers(any()) } returns requestHeadersSpec
            every { requestHeadersSpec.retrieve() } returns responseSpec
            every { responseSpec.bodyToMono<BulkTilgangsResponse>() } returns Mono.just(expectedResponse)
        }
    }
}
