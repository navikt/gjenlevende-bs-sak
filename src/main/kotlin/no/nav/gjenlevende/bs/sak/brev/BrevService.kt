package no.nav.gjenlevende.bs.sak.brev

import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BrevService(
    private val brevRepository: BrevRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun opprettBrev(
        behandlingId: UUID,
        brevRequest: BrevRequest,
    ) {
        val brev =
            Brev(
                behandlingId = behandlingId,
                brevJson = brevRequest,
            )

        if (brevRepository.existsById(behandlingId)) {
            brevRepository.update(brev)
        } else {
            brevRepository.insert(brev)
        }
    }

    fun hentBrev(behandlingId: UUID): Brev? = brevRepository.findByIdOrNull(behandlingId)
}
