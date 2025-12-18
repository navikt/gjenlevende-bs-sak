package no.nav.gjenlevende.bs.sak.pdl

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nav.gjenlevende.bs.sak.ApplicationLocal
import org.assertj.core.api.Assertions
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
    PdlController::class,
)
@ContextConfiguration(classes = [ApplicationLocal::class])
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integrasjonstest")
open class PdlControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var pdlService: PdlService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `hentNavn returnerer 200 OK med navn når person finnes`() {
        val fagsakPersonId = UUID.randomUUID()

        val hentNavnRequest = HentNavnRequest(fagsakPersonId)

        every {
            pdlService.hentNavn(fagsakPersonId)
        } returns Navn("fornavn", null, "etternavn")

        val responseJson =
            mockMvc
                .post("/api/pdl/navn") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(hentNavnRequest)
                }.andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                }.andReturn()
                .response.contentAsString

        val response = objectMapper.readValue<Navn>(responseJson)
        Assertions.assertThat(response.fornavn).isEqualTo("fornavn")
        Assertions.assertThat(response.etternavn).isEqualTo("etternavn")

        verify(exactly = 1) {
            pdlService.hentNavn(fagsakPersonId)
        }
    }

    @Test
    fun `returnerer 400 ved ugyldig request`() {
        mockMvc
            .post("/api/pdl/navn") {
                contentType = MediaType.APPLICATION_JSON
                content = "ugyldig request"
            }.andExpect {
                status { isBadRequest() }
            }

        verify(exactly = 0) {
            pdlService.hentNavn(any())
        }
    }
}
