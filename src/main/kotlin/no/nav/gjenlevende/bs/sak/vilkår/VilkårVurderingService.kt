package no.nav.gjenlevende.bs.sak.vilkår

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class VilkårVurderingService(
    private val vilkårVurderingRepository: VilkårVurderingRepository,
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

        val resultat =
            if (eksisterendeVurdering == null) {
                vilkårVurderingRepository.insert(
                    VilkårVurdering(
                        behandlingId = behandlingId,
                        vilkårType = request.vilkårType,
                        vurdering = request.vurdering,
                        begrunnelse = request.begrunnelse,
                    ),
                )
            } else {
                vilkårVurderingRepository.update(
                    eksisterendeVurdering.copy(
                        vurdering = request.vurdering,
                        begrunnelse = request.begrunnelse,
                    ),
                )
            }

        return resultat
    }
}
