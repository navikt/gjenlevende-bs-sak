package no.nav.gjenlevende.bs.sak.vilkår

import no.nav.gjenlevende.bs.sak.endringshistorikk.EndringType
import no.nav.gjenlevende.bs.sak.endringshistorikk.EndringshistorikkService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class VilkårVurderingService(
    private val vilkårVurderingRepository: VilkårVurderingRepository,
    private val endringshistorikkService: EndringshistorikkService,
) {
    fun hentVilkårVurderinger(behandlingId: UUID): List<VilkårVurdering> = vilkårVurderingRepository.findByBehandlingId(behandlingId)

    fun lagreVilkårVurdering(
        behandlingId: UUID,
        request: VilkårVurderingRequest,
    ): VilkårVurdering {
        val eksisterendeVurdering =
            vilkårVurderingRepository.findByBehandlingIdAndVilkårType(
                behandlingId = behandlingId,
                vilkårType = request.vilkårType,
            )

        if (eksisterendeVurdering == null) {
            val vurdering =
                vilkårVurderingRepository.insert(
                    VilkårVurdering(
                        behandlingId = behandlingId,
                        vilkårType = request.vilkårType,
                        vurdering = request.vurdering,
                        begrunnelse = request.begrunnelse,
                    ),
                )
            endringshistorikkService.registrerEndring(
                behandlingId = behandlingId,
                endringType = EndringType.VILKÅR_VURDERING_OPPRETTET,
                detaljer = "${request.vilkårType}: ${request.vurdering}",
            )
            return vurdering
        }

        val oppdatert =
            vilkårVurderingRepository.update(
                eksisterendeVurdering.copy(
                    vurdering = request.vurdering,
                    begrunnelse = request.begrunnelse,
                ),
            )
        endringshistorikkService.registrerEndring(
            behandlingId = behandlingId,
            endringType = EndringType.VILKÅR_VURDERING_OPPDATERT,
            detaljer = "${request.vilkårType}: ${request.vurdering}",
        )
        return oppdatert
    }
}
