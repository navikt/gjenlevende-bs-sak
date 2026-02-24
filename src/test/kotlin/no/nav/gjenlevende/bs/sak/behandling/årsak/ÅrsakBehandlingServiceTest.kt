package no.nav.gjenlevende.bs.sak.behandling.årsak

import io.mockk.every
import io.mockk.mockk
import no.nav.gjenlevende.bs.sak.behandling.BehandlingService
import no.nav.gjenlevende.bs.sak.endringshistorikk.EndringshistorikkService
import no.nav.gjenlevende.bs.sak.infrastruktur.exception.Feil
import org.assertj.core.api.Assertions.assertThatThrownBy
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test

class ÅrsakBehandlingServiceTest {
    private val årsakBehandlingRepository = mockk<ÅrsakBehandlingRepository>(relaxed = true)
    private val behandlingService = mockk<BehandlingService>(relaxed = true)
    private val endringshistorikkService = mockk<EndringshistorikkService>(relaxed = true)
    private val årsakBehandlingService = ÅrsakBehandlingService(årsakBehandlingRepository, behandlingService, endringshistorikkService)

    @Test
    fun `lagreÅrsakForBehandling kaster feil når behandling ikke er redigerbar`() {
        val behandlingId = UUID.randomUUID()
        val request =
            ÅrsakBehandlingRequest(
                kravdato = LocalDate.now(),
                årsak = Årsak.SØKNAD,
                beskrivelse = "Test",
            )

        every { behandlingService.validerBehandlingErRedigerbar(behandlingId) } throws Feil("Behandlingen er ikke redigerbar. Status: FATTER_VEDTAK")

        assertThatThrownBy { årsakBehandlingService.lagreÅrsakForBehandling(behandlingId, request) }
            .isInstanceOf(Feil::class.java)
            .hasMessageContaining("Behandlingen er ikke redigerbar")
    }
}
