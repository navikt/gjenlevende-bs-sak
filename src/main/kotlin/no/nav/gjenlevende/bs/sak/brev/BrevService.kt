package no.nav.gjenlevende.bs.sak.brev

import no.nav.familie.prosessering.domene.Task
import no.nav.gjenlevende.bs.sak.task.BrevTask
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper

@Service
class BrevService(
    private val objectMapper: ObjectMapper,
) {
    fun lagBrevPDFtask(brevRequest: BrevRequest): Task {
        val payload = objectMapper.writeValueAsString(brevRequest)
        return BrevTask.opprettTask(payload)
    }
}
