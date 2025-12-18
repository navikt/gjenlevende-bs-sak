package no.nav.gjenlevende.bs.sak.brev

import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest
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
    @Transactional
    fun opprettBrev(
        behandlingsId: UUID,
        brevRequest: BrevRequest,
    ): Brev {
        val logger = LoggerFactory.getLogger(javaClass)
        val brev =
            Brev(
                behandlingsId = behandlingsId,
                brevJson = objectMapper.writeValueAsString(brevRequest),
            )

        logger.info("opprettetAv={}, endretAv={}", brev.sporbar.opprettetAv, brev.sporbar.endret.endretAv)

        try {
            brevRepository.insert(brev)
        } catch (e: Exception) {
            logger.error(
                "Noe gikk galt",
                brev,
                behandlingsId,
                brev.sporbar.opprettetAv,
                brev.sporbar.endret.endretAv,
                e,
            )
            throw e
        }
        return brev
    }

    fun hentBrev(behandlingsId: UUID): Brev? = brevRepository.findByIdOrNull(behandlingsId)
}
