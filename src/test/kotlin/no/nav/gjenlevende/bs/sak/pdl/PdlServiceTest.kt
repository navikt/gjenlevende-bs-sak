package no.nav.gjenlevende.bs.sak.pdl

import io.mockk.every
import io.mockk.mockk
import no.nav.gjenlevende.bs.sak.fagsak.FagsakPersonService
import org.assertj.core.api.Assertions.assertThat
import org.springframework.core.ParameterizedTypeReference
import java.util.UUID
import kotlin.test.Test

class PdlServiceTest {
    val pdlClient = mockk<PdlClient>()
    val fagsakPersonService = mockk<FagsakPersonService>()
    val pdlService = PdlService(pdlClient, fagsakPersonService)

    @Test
    fun `hent første navn ved response fra pdl`() {
        every { fagsakPersonService.hentAktivIdent(any()) } returns "01010199999"

        every { pdlClient.utførQuery(any(), any(), any<ParameterizedTypeReference<PdlResponse<HentPersonData>>>(), any()) } returns
            HentPersonData(
                hentPerson =
                    HentPerson(
                        navn =
                            listOf(
                                Navn("Fornavn", null, "Etternavn"),
                                Navn("Fornavn2", null, "Etternavn2"),
                            ),
                    ),
            )

        val fagsakPersonId = UUID.randomUUID()

        val navn = pdlService.hentNavn(fagsakPersonId)

        assertThat(navn?.fornavn).isEqualTo("Fornavn")
        assertThat(navn?.etternavn).isEqualTo("Etternavn")
    }
}
