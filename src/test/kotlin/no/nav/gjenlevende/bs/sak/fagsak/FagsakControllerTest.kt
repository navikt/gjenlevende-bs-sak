package no.nav.gjenlevende.bs.sak.fagsak

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import no.nav.familie.prosessering.rest.Ressurs
import no.nav.gjenlevende.bs.sak.ApplicationLocal
import no.nav.gjenlevende.bs.sak.fagsak.dto.FagsakDto
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.TilgangService
import no.nav.gjenlevende.bs.sak.infotrygd.dto.StønadType
import org.assertj.core.api.Assertions.assertThat
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
import tools.jackson.module.kotlin.readValue
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

    @MockkBean
    private lateinit var fagsakPersonService: FagsakPersonService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `skal returnere fagsak når person finnes`() {
        val personident = "12345678910"
        val stønadstype = StønadType.BARNETILSYN
        val fagsakRequest =
            FagsakRequest(
                personident = personident,
                fagsakPersonId = null,
                stønadstype = stønadstype,
            )

        val forventetFagsak =
            FagsakDto(
                personident = personident,
                stønadstype = stønadstype,
                id = UUID.randomUUID(),
                fagsakPersonId = UUID.randomUUID(),
                eksternId = 1L,
            )

        every {
            fagsakService.hentEllerOpprettFagsakMedBehandlinger(personident, stønadstype)
        } returns forventetFagsak

        justRun { tilgangService.validerTilgangTilPersonMedBarn(any()) }

        val responseJson =
            mockMvc
                .post("/api/fagsak") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(fagsakRequest)
                }.andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                }.andReturn()
                .response.contentAsString

        val ressursFagsakDto: Ressurs<FagsakDto> = objectMapper.readValue(responseJson)
        assertThat(ressursFagsakDto.data?.personident).isEqualTo(personident)
        assertThat(ressursFagsakDto.data?.stønadstype).isEqualTo(stønadstype)

        verify(exactly = 1) {
            fagsakService.hentEllerOpprettFagsakMedBehandlinger(personident, stønadstype)
        }
    }

    @Test
    fun `skal returnere fagsak når fagsakPersonId finnes`() {
        val fagsakPersonId = UUID.randomUUID()
        val personident = "12345678910"
        val stønadstype = StønadType.BARNETILSYN
        val fagsakRequest =
            FagsakRequest(
                personident = null,
                fagsakPersonId = fagsakPersonId,
                stønadstype = stønadstype,
            )

        val forventetFagsak =
            FagsakDto(
                personident = personident,
                stønadstype = stønadstype,
                id = UUID.randomUUID(),
                fagsakPersonId = fagsakPersonId,
                eksternId = 1L,
            )

        every {
            fagsakService.hentEllerOpprettFagsakMedFagsakPersonId(fagsakPersonId, stønadstype)
        } returns forventetFagsak

        every { fagsakPersonService.hentAktivIdent(any()) } returns personident

        justRun { tilgangService.validerTilgangTilPersonMedBarn(any()) }

        val responseJson =
            mockMvc
                .post("/api/fagsak") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(fagsakRequest)
                }.andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                }.andReturn()
                .response.contentAsString

        val ressursFagsakDto: Ressurs<FagsakDto> = objectMapper.readValue(responseJson)
        assertThat(ressursFagsakDto.data?.fagsakPersonId).isEqualTo(fagsakPersonId)
        assertThat(ressursFagsakDto.data?.stønadstype).isEqualTo(stønadstype)

        verify(exactly = 1) {
            fagsakService.hentEllerOpprettFagsakMedFagsakPersonId(fagsakPersonId, stønadstype)
        }
    }

    @Test
    fun `skal håndtere valideringsfeil`() {
        val ugyldigRequest = """{"personident": ""}"""

        mockMvc
            .post("/api/fagsak") {
                contentType = MediaType.APPLICATION_JSON
                content = ugyldigRequest
            }.andExpect {
                status { isBadRequest() }
            }
    }
}
