package no.nav.gjenlevende.bs.sak.brev

import no.nav.familie.prosessering.domene.Task
import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest
import no.nav.gjenlevende.bs.sak.task.BrevTask
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BrevService(
    private val brevRepository: BrevRepository,
) {
    fun lagBrevPDFtask(behandlingsId: UUID): Task = BrevTask.opprettTask(behandlingsId.toString())

    @Transactional
    fun opprettBrev(
        behandlingsId: UUID,
        brevRequest: BrevRequest,
    ): Brev {
        val brev =
            Brev(
                behandlingsId = behandlingsId,
                brevJson = brevRequest,
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
