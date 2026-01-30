package no.nav.gjenlevende.bs.sak.vilkår

import no.nav.gjenlevende.bs.sak.felles.sikkerhet.Tilgangskontroll
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class VilkårVurderingService(
    private val vilkårVurderingRepository: VilkårVurderingRepository,
) {
    @Tilgangskontroll
    fun hentVilkårVurderinger(behandlingId: UUID): List<VilkårVurdering> = vilkårVurderingRepository.findByBehandlingId(behandlingId)

    @Tilgangskontroll
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
            return vilkårVurderingRepository.insert(
                VilkårVurdering(
                    behandlingId = behandlingId,
                    vilkårType = request.vilkårType,
                    vurdering = request.vurdering,
                    begrunnelse = request.begrunnelse,
                ),
            )
        }

        return vilkårVurderingRepository.update(
            eksisterendeVurdering.copy(
                vurdering = request.vurdering,
                begrunnelse = request.begrunnelse,
            ),
        )
    }
}
