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
    fun lagBrevPdfTask(behandlingId: UUID): Task =
        BrevTask.opprettTask(
            objectMapper.writeValueAsString(
                BrevTask.LagBrevPdfTaskData(behandlingId),
            ),
        )

    @Transactional
    fun mellomlagreBrev(
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
    fun oppdatereBrevPdf(
        behandlingId: UUID,
        pdf: ByteArray,
    ) {
        val eksisterendeBrev =
            brevRepository.findByIdOrNull(behandlingId)
                ?: error("Fant ikke brev for behandlingId=$behandlingId ved lagring av PDF")
        val oppdatertBrevPdf = eksisterendeBrev.copy(brevPdf = pdf)
        brevRepository.update(oppdatertBrevPdf)
    }

    @Transactional
    fun oppdaterSaksbehandler(
        behandlingId: UUID,
        saksbehandler: String?,
    ) {
        val eksisterendeBrev =
            brevRepository.findByIdOrNull(behandlingId)
                ?: error("Fant ikke brev for behandlingId=$behandlingId ved oppdatering av saksbehandler")
        val oppdatert = eksisterendeBrev.copy(saksbehandler = saksbehandler)
        brevRepository.update(oppdatert)
    }

    @Transactional
    fun oppdaterBeslutter(
        behandlingId: UUID,
        beslutter: String?,
    ) {
        val eksisterendeBrev =
            brevRepository.findByIdOrNull(behandlingId)
                ?: error("Fant ikke brev for behandlingId=$behandlingId ved oppdatering av beslutter")
        val oppdatert = eksisterendeBrev.copy(beslutter = beslutter)
        brevRepository.update(oppdatert)
    }
}
