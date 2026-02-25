package no.nav.gjenlevende.bs.sak.brev

import io.mockk.every
import io.mockk.mockk
import no.nav.gjenlevende.bs.sak.behandling.BehandlingService
import no.nav.gjenlevende.bs.sak.brev.domain.Brevmottaker
import no.nav.gjenlevende.bs.sak.brev.domain.BrevmottakerRolle
import no.nav.gjenlevende.bs.sak.brev.domain.MottakerType
import no.nav.gjenlevende.bs.sak.infrastruktur.exception.Feil
import org.assertj.core.api.Assertions.assertThatThrownBy
import java.util.UUID
import kotlin.test.Test

class BrevmottakerServiceTest {
    private val brevmottakerRepository = mockk<BrevmottakerRepository>(relaxed = true)
    private val behandlingService = mockk<BehandlingService>(relaxed = true)
    private val brevmottakerService = BrevmottakerService(brevmottakerRepository, behandlingService)

    @Test
    fun `oppdaterBrevmottakere kaster feil når behandling ikke er redigerbar`() {
        val behandlingId = UUID.randomUUID()
        val brevmottakere =
            listOf(
                Brevmottaker(
                    behandlingId = behandlingId,
                    personRolle = BrevmottakerRolle.BRUKER,
                    mottakerType = MottakerType.PERSON,
                    personident = "12345678910",
                ),
            )

        every { behandlingService.validerBehandlingErRedigerbar(behandlingId) } throws Feil("Behandlingen er ikke redigerbar. Status: FATTER_VEDTAK")

        assertThatThrownBy { brevmottakerService.oppdaterBrevmottakere(behandlingId, brevmottakere) }
            .isInstanceOf(Feil::class.java)
            .hasMessageContaining("Behandlingen er ikke redigerbar")
    }
}
