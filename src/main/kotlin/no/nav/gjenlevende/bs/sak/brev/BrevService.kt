package no.nav.gjenlevende.bs.sak.brev

import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@Service
class BrevService(
    private val brevRepository: BrevRepository,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun opprettBrev(
        behandlingsId: UUID,
        brevRequest: BrevRequest,
    ): Brev {
        val brev =
            Brev(
                behandlingsId = behandlingsId,
                brevJson = objectMapper.writeValueAsString(brevRequest),
            )

        brevRepository.insert(brev)
        return brev
    }

    fun hentBrev(behandlingsId: UUID): Brev? = brevRepository.findByIdOrNull(behandlingsId)
}
