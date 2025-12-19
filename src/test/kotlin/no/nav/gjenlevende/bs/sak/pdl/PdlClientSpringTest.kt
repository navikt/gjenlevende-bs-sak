package no.nav.gjenlevende.bs.sak.pdl

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.gjenlevende.bs.sak.felles.OAuth2RestOperationsFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestTemplate
import java.net.URI

@SpringBootTest(
    classes = [PdlClient::class],
    properties = [
        "PDL_URL=http://localhost:9999",
        "pdl.oauth.registration-id=pdl-clientcredentials",
    ],
)
class PdlClientSpringTest {
    @MockkBean(relaxed = true)
    private lateinit var oAuth2RestOperationsFactory: OAuth2RestOperationsFactory

    private lateinit var restTemplate: RestTemplate
    private lateinit var mockServer: MockRestServiceServer

    @BeforeEach
    fun setup() {
        restTemplate = RestTemplate()
        every { oAuth2RestOperationsFactory.create(any(), any()) } returns restTemplate

        mockServer = MockRestServiceServer.createServer(restTemplate)
    }

    @AfterEach
    fun tearDown() {
        mockServer.verify()
    }

    @Test
    fun `utførQuery returnerer data ved gyldig respons`() {
        val jsonResponse =
            """
            {
              "data": {
                "hentPerson": {
                  "navn": [
                    { "fornavn": "Fornavn", "mellomnavn": null, "etternavn": "Etternavn" }
                  ]
                }
              }
            }
            """.trimIndent()

        mockServer
            .expect(requestTo("http://localhost:9999/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
            .andExpect(header("Tema", "EYO"))
            .andExpect(header("behandlingsnummer", "B373"))
            .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON))

        val result =
            PdlClient(
                pdlUrl = URI("http://localhost:9999"),
                registrationId = "pdl-clientcredentials",
                oauth2RestFactory = oAuth2RestOperationsFactory,
            ).utførQuery(
                query = "query {}",
                variables = mapOf("ident" to "123"),
                responstype = object : ParameterizedTypeReference<PdlResponse<HentPersonData>>() {},
                operasjon = "hentNavn",
            )

        assertThat(result).isNotNull
        assertThat(
            result
                ?.hentPerson
                ?.navn
                ?.first()
                ?.fornavn,
        ).isEqualTo("Fornavn")
        assertThat(
            result
                ?.hentPerson
                ?.navn
                ?.first()
                ?.etternavn,
        ).isEqualTo("Etternavn")
    }

    @Test
    fun `utførQuery kaster PdlException ved teknisk feil`() {
        mockServer
            .expect(requestTo("http://localhost:9999/graphql"))
            .andRespond(withServerError())

        val pdlClient =
            PdlClient(
                pdlUrl = URI("http://localhost:9999"),
                registrationId = "pdl-clientcredentials",
                oauth2RestFactory = oAuth2RestOperationsFactory,
            )

        assertThrows<PdlException> {
            pdlClient.utførQuery(
                query = "query {}",
                variables = emptyMap(),
                responstype = object : ParameterizedTypeReference<PdlResponse<HentPersonData>>() {},
                operasjon = "hentNavn",
            )
        }
    }
}
