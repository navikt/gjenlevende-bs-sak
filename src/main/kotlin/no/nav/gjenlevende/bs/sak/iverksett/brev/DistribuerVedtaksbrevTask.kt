package no.nav.gjenlevende.bs.sak.iverksett.brev

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.gjenlevende.bs.sak.iverksett.DokarkivClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = DistribuerVedtaksbrevTask.TYPE,
    maxAntallFeil = DistribuerVedtaksbrevTask.MAX_FORSØK,
    settTilManuellOppfølgning = true,
    triggerTidVedFeilISekunder = 15 * 60L,
    beskrivelse = "Distribuerer vedtaksbrev.",
)
class DistribuerVedtaksbrevTask(
    private val dokarkivClient: DokarkivClient,
//    private val iverksettResultatService: IverksettResultatService,
    private val taskService: TaskService,
) : AsyncTaskStep {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val resultat = distribuerVedtaksbrev(behandlingId)
    }

    companion object {
        const val TYPE = "distribuerVedtaksbrev"
        const val MAX_FORSØK = 50
    }
}
