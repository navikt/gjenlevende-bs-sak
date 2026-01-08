package no.nav.gjenlevende.bs.sak.brev

import no.nav.familie.prosessering.domene.Task
import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest
import no.nav.gjenlevende.bs.sak.task.BrevTask
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
    fun lagBrevPDFtask(behandlingId: UUID): Task =
        BrevTask.opprettTask(
            objectMapper.writeValueAsString(
                BrevTask.LagBrevPdfTaskData(behandlingId),
            ),
        )

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

    @Transactional
    fun lagreBrevPdf(
        behandlingId: UUID,
        pdf: ByteArray,
    ) {
        val eksisterende =
            brevRepository.findByIdOrNull(behandlingId)
                ?: error("Fant ikke brev for behandlingId=$behandlingId ved lagring av PDF")
        brevRepository.update(eksisterende.copy(brevPdf = pdf))
    }
}
