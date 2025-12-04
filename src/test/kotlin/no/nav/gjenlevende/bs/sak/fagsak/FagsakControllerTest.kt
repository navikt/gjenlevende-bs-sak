package no.nav.gjenlevende.bs.sak.fagsak

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nav.gjenlevende.bs.sak.ApplicationLocal
import no.nav.gjenlevende.bs.sak.fagsak.dto.FagsakDto
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.TilgangService
import no.nav.gjenlevende.bs.sak.infotrygd.dto.StønadType
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@WebMvcTest(
    FagsakController::class,
)
@ContextConfiguration(classes = [ApplicationLocal::class])
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integrasjonstest")
open class FagsakControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var fagsakService: FagsakService

    @MockkBean
    private lateinit var tilgangService: TilgangService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `skal returnere fagsak når person finnes`() {
        // Arrange
        val personIdent = "12345678910"
        val stønadstype = StønadType.BARNETILSYN
        val fagsakRequest =
            FagsakRequest(
                personIdent = personIdent,
                stønadstype = stønadstype,
            )

        val forventetFagsak =
            FagsakDto(
                personIdent = personIdent,
                stønadstype = stønadstype,
                id = UUID.randomUUID(),
                fagsakPersonId = UUID.randomUUID(),
                eksternId = 1L,
            )

        every {
            fagsakService.hentEllerOpprettFagsakMedBehandlinger(personIdent, stønadstype)
        } returns forventetFagsak

        // Act & Assert
        mockMvc
            .post("/api/fagsak") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(fagsakRequest)
            }.andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.status") { value("SUKSESS") }
                jsonPath("$.data.personIdent") { value(personIdent) }
                jsonPath("$.data.stønadstype") { value(stønadstype.toString()) }
            }

        verify(exactly = 1) {
            fagsakService.hentEllerOpprettFagsakMedBehandlinger(personIdent, stønadstype)
        }
    }

    @Test
    fun `skal håndtere valideringsfeil`() {
        val ugyldigRequest = """{"personIdent": ""}"""

        mockMvc
            .post("/api/fagsak") {
                contentType = MediaType.APPLICATION_JSON
                content = ugyldigRequest
            }.andExpect {
                status { isBadRequest() }
            }
    }
}
