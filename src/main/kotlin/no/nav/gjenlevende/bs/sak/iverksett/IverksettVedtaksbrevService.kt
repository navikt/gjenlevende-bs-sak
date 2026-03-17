package no.nav.gjenlevende.bs.sak.iverksett

import no.nav.familie.prosessering.domene.Task
import no.nav.gjenlevende.bs.sak.iverksett.brev.DistribuerVedtaksbrevTask
import no.nav.gjenlevende.bs.sak.iverksett.brev.DistribuerVedtaksbrevTaskData
import no.nav.gjenlevende.bs.sak.iverksett.brev.JournalførVedtaksbrevTask
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@Service
class IverksettVedtaksbrevService(
    private val objectMapper: ObjectMapper,
) {
    fun opprettIverksettVedtaksbrevTask(behandlingId: UUID): Task =
        JournalførVedtaksbrevTask.opprettTask(
            objectMapper.writeValueAsString(
                JournalførVedtaksbrevTask.JournalførVedtaksbrevTaskData(behandlingId),
            ),
        )

    fun opprettDistribuerVedtaksbrevTask(behandlingId: UUID): Task =
        DistribuerVedtaksbrevTask.opprettTask(
            objectMapper.writeValueAsString(
                DistribuerVedtaksbrevTask.DistribuerVedtaksbrevTaskData(behandlingId),
            ),
        )
}
