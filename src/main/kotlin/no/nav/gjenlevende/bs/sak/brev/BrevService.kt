package no.nav.gjenlevende.bs.sak.brev

import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest
import org.postgresql.util.PGobject
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun opprettBrev(
        behandlingsId: UUID,
        brevRequest: BrevRequest,
    ): Brev {
        val jsonb =
            PGobject().apply {
                type = "jsonb"
                value = objectMapper.writeValueAsString(brevRequest)
            }
        val brev =
            Brev(
                behandlingsId = behandlingsId,
                brevJson = jsonb,
            )

        if (brevRepository.existsById(behandlingsId)) {
            brevRepository.update(brev)
        } else {
            brevRepository.insert(brev)
        }

        return brev
    }

    fun hentBrev(behandlingsId: UUID): Brev? = brevRepository.findByIdOrNull(behandlingsId)
}
