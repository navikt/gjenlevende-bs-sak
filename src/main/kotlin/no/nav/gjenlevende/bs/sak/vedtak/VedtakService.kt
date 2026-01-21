package no.nav.gjenlevende.bs.sak.vedtak

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class VedtakService(
    private val vedtakRepository: VedtakRepository,
) {
    fun hentVedtak(behandlingId: UUID): Vedtak? = vedtakRepository.findByIdOrNull(behandlingId)

    fun lagreVedtak(
        vedtakDto: VedtakDto,
        behandlingId: UUID,
    ): UUID = vedtakRepository.insert(vedtakDto.tilVedtak(behandlingId)).behandlingId
}
