package no.nav.gjenlevende.bs.sak.vilkår

import io.mockk.every
import io.mockk.mockk
import no.nav.gjenlevende.bs.sak.behandling.BehandlingService
import no.nav.gjenlevende.bs.sak.endringshistorikk.EndringshistorikkService
import no.nav.gjenlevende.bs.sak.infrastruktur.exception.Feil
import org.assertj.core.api.Assertions.assertThatThrownBy
import java.util.UUID
import kotlin.test.Test

class VilkårVurderingServiceTest {
    private val vilkårVurderingRepository = mockk<VilkårVurderingRepository>(relaxed = true)
    private val behandlingService = mockk<BehandlingService>(relaxed = true)
    private val endringshistorikkService = mockk<EndringshistorikkService>(relaxed = true)
    private val vilkårVurderingService = VilkårVurderingService(vilkårVurderingRepository, behandlingService, endringshistorikkService)

    @Test
    fun `lagreVilkårVurdering kaster feil når behandling ikke er redigerbar`() {
        val behandlingId = UUID.randomUUID()
        val request =
            VilkårVurderingRequest(
                vilkårType = VilkårType.INNGANGSVILKÅR,
                vurdering = Vurdering.JA,
                begrunnelse = "Test",
            )

        every { behandlingService.validerBehandlingErRedigerbar(behandlingId) } throws Feil("Behandlingen er ikke redigerbar. Status: FATTER_VEDTAK")

        assertThatThrownBy { vilkårVurderingService.lagreVilkårVurdering(behandlingId, request) }
            .isInstanceOf(Feil::class.java)
            .hasMessageContaining("Behandlingen er ikke redigerbar")
    }
}
