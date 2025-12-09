// src/test/kotlin/no/nav/gjenlevende/bs/sak/opplysninger/FamilieIntegrasjonerClientTest.kt

package no.nav.gjenlevende.bs.sak.opplysninger

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.gjenlevende.bs.sak.fagsak.domain.PersonIdent
import no.nav.gjenlevende.bs.sak.felles.OAuth2RestOperationsFactory
import no.nav.gjenlevende.bs.sak.felles.auditlogger.Tilgang
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestOperations
import java.net.URI

class FamilieIntegrasjonerClientTest {
    private val baseUri = URI.create("http://localhost")
    private val registrationId = "familie-integrasjoner-clientcredentials"

    private val oauth2RestFactory = mockk<OAuth2RestOperationsFactory>()
    private val restOperations = mockk<RestOperations>()

    private lateinit var client: FamilieIntegrasjonerClient

    @BeforeEach
    fun setup() {
        every { oauth2RestFactory.create(registrationId, any()) } returns restOperations

        client =
            FamilieIntegrasjonerClient(
                integrasjonUri = baseUri,
                registrationId = registrationId,
                oauth2RestFactory = oauth2RestFactory,
            )
    }

    @Test
    fun `sjekkTilgangTilPersonMedRelasjoner returnerer Tilgang når respons body finnes`() {
        val personIdent = "01010112345"
        val forventetTilgang = Tilgang(true)

        every {
            restOperations.exchange(
                any<URI>(),
                HttpMethod.POST,
                any<HttpEntity<PersonIdent>>(),
                any<ParameterizedTypeReference<Tilgang>>(),
            )
        } returns ResponseEntity.ok(forventetTilgang)

        val faktisk = client.sjekkTilgangTilPersonMedRelasjoner(personIdent)

        assertEquals(forventetTilgang, faktisk)

        verify {
            restOperations.exchange(
                any<URI>(),
                HttpMethod.POST,
                any<HttpEntity<PersonIdent>>(),
                any<ParameterizedTypeReference<Tilgang>>(),
            )
        }
    }

    @Test
    fun `sjekkTilgangTilPersonMedRelasjoner kaster når respons body er null`() {
        val personIdent = "02020200000"

        every {
            restOperations.exchange(
                any<URI>(),
                HttpMethod.POST,
                any<HttpEntity<PersonIdent>>(),
                any<ParameterizedTypeReference<Tilgang>>(),
            )
        } returns ResponseEntity.ok(null)

        assertThrows(IllegalStateException::class.java) {
            client.sjekkTilgangTilPersonMedRelasjoner(personIdent)
        }

        verify {
            restOperations.exchange(
                any<URI>(),
                HttpMethod.POST,
                any<HttpEntity<PersonIdent>>(),
                any<ParameterizedTypeReference<Tilgang>>(),
            )
        }
    }
}
