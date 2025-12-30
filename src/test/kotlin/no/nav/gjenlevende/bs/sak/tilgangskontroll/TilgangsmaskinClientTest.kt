package no.nav.gjenlevende.bs.sak.tilgangskontroll

import io.mockk.every
import io.mockk.mockk
import no.nav.gjenlevende.bs.sak.felles.OAuth2RestOperationsFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestOperations
import java.net.URI
import java.nio.charset.StandardCharsets

class TilgangsmaskinClientTest {
    private val tilgangsmaskinUrl = URI.create("http://localhost:8080")
    private val registrationId = "tilgangsmaskin"

    private val oauth2RestFactory = mockk<OAuth2RestOperationsFactory>()
    private val restOperations = mockk<RestOperations>()

    private lateinit var client: TilgangsmaskinClient

    @BeforeEach
    fun setup() {
        every { oauth2RestFactory.create(registrationId) } returns restOperations

        client =
            TilgangsmaskinClient(
                tilgangsmaskinUrl = tilgangsmaskinUrl,
                registrationId = registrationId,
                oauth2RestFactory = oauth2RestFactory,
            )
    }

    @Nested
    inner class SjekkTilgangEnkel {
        @Test
        fun `saksbehandler har tilgang til vanlig bruker - returnerer 204`() {
            val navIdent = "Z123456"
            val personident = "12345678901"

            every {
                restOperations.exchange(
                    any<URI>(),
                    HttpMethod.GET,
                    any<HttpEntity<Any>>(),
                    any<ParameterizedTypeReference<String>>(),
                )
            } returns ResponseEntity.noContent().build()

            val resultat = client.sjekkTilgangEnkel(navIdent, personident)

            assertTrue(resultat.harTilgang)
            assertEquals(navIdent, resultat.navIdent)
            assertEquals(personident, resultat.personident)
            assertNull(resultat.avvisningsgrunn)
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
                restOperations.exchange(
                    any<URI>(),
                    HttpMethod.GET,
                    any<HttpEntity<Any>>(),
                    any<ParameterizedTypeReference<String>>(),
                )
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
            assertEquals("AVVIST_HABILITET", resultat.avvisningsgrunn)
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
                restOperations.exchange(
                    any<URI>(),
                    HttpMethod.GET,
                    any<HttpEntity<Any>>(),
                    any<ParameterizedTypeReference<String>>(),
                )
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
            assertEquals("AVVIST_HABILITET", resultat.avvisningsgrunn)
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
                restOperations.exchange(
                    any<URI>(),
                    HttpMethod.GET,
                    any<HttpEntity<Any>>(),
                    any<ParameterizedTypeReference<String>>(),
                )
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
            assertEquals("AVVIST_HABILITET", resultat.avvisningsgrunn)
        }

        @Test
        fun `saksbehandler med riktig gruppe har tilgang til strengt fortrolig bruker`() {
            val navIdent = "Z123456"
            val personident = "12345678904"

            every {
                restOperations.exchange(
                    any<URI>(),
                    HttpMethod.GET,
                    any<HttpEntity<Any>>(),
                    any<ParameterizedTypeReference<String>>(),
                )
            } returns ResponseEntity.noContent().build()

            val resultat = client.sjekkTilgangEnkel(navIdent, personident)

            assertTrue(resultat.harTilgang)
            assertNull(resultat.avvisningsgrunn)
        }

        @Test
        fun `saksbehandler med riktig gruppe har tilgang til fortrolig bruker`() {
            val navIdent = "Z123456"
            val personident = "12345678905"

            every {
                restOperations.exchange(
                    any<URI>(),
                    HttpMethod.GET,
                    any<HttpEntity<Any>>(),
                    any<ParameterizedTypeReference<String>>(),
                )
            } returns ResponseEntity.noContent().build()

            val resultat = client.sjekkTilgangEnkel(navIdent, personident)

            assertTrue(resultat.harTilgang)
            assertNull(resultat.avvisningsgrunn)
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
                restOperations.exchange(
                    any<URI>(),
                    HttpMethod.GET,
                    any<HttpEntity<Any>>(),
                    any<ParameterizedTypeReference<String>>(),
                )
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
            assertEquals("AVVIST_STRENGT_FORTROLIG_ADRESSE", resultat.avvisningsgrunn)
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
                restOperations.exchange(
                    any<URI>(),
                    HttpMethod.GET,
                    any<HttpEntity<Any>>(),
                    any<ParameterizedTypeReference<String>>(),
                )
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
            assertEquals("AVVIST_FORTROLIG_ADRESSE", resultat.avvisningsgrunn)
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
                restOperations.exchange(
                    any<URI>(),
                    HttpMethod.GET,
                    any<HttpEntity<Any>>(),
                    any<ParameterizedTypeReference<String>>(),
                )
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
            assertEquals("AVVIST_GEOGRAFISK", resultat.avvisningsgrunn)
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
                restOperations.exchange(
                    any<URI>(),
                    HttpMethod.GET,
                    any<HttpEntity<Any>>(),
                    any<ParameterizedTypeReference<String>>(),
                )
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
            assertEquals("AVVIST_SKJERMING", resultat.avvisningsgrunn)
            assertEquals("Du har ikke tilgang til Nav-ansatte og andre skjermede brukere", resultat.begrunnelse)
        }

        @Test
        fun `saksbehandler med tilgang til egen ansatt har tilgang`() {
            val navIdent = "Z654321"
            val personident = "12345678901"

            every {
                restOperations.exchange(
                    any<URI>(),
                    HttpMethod.GET,
                    any<HttpEntity<Any>>(),
                    any<ParameterizedTypeReference<String>>(),
                )
            } returns ResponseEntity.noContent().build()

            val resultat = client.sjekkTilgangEnkel(navIdent, personident)

            assertTrue(resultat.harTilgang)
            assertEquals(navIdent, resultat.navIdent)
            assertEquals(personident, resultat.personident)
            assertNull(resultat.avvisningsgrunn)
            assertNull(resultat.begrunnelse)
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
                restOperations.exchange(
                    any<URI>(),
                    HttpMethod.GET,
                    any<HttpEntity<Any>>(),
                    any<ParameterizedTypeReference<String>>(),
                )
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
            assertEquals("AVVIST_SKJERMING", resultat.avvisningsgrunn)
            assertEquals("Du har ikke tilgang til Nav-ansatte og andre skjermede brukere", resultat.begrunnelse)
        }
    }
}
