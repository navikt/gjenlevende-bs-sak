package no.nav.gjenlevende.bs.sak.iverksett.brev

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.gjenlevende.bs.sak.behandling.BehandlingService
import no.nav.gjenlevende.bs.sak.brev.BrevService
import no.nav.gjenlevende.bs.sak.fagsak.FagsakPersonService
import no.nav.gjenlevende.bs.sak.fagsak.FagsakRepository
import no.nav.gjenlevende.bs.sak.iverksett.DokarkivClient
import no.nav.gjenlevende.bs.sak.iverksett.domene.ArkiverDokumentRequest
import no.nav.gjenlevende.bs.sak.iverksett.domene.Dokument
import no.nav.gjenlevende.bs.sak.iverksett.domene.Dokumenttype
import no.nav.gjenlevende.bs.sak.iverksett.domene.Filtype
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = JournalførVedtaksbrevTask.TYPE,
    maxAntallFeil = 5,
    triggerTidVedFeilISekunder = 15,
    beskrivelse = "Journalfører vedtaksbrev.",
)
class JournalførVedtaksbrevTask(
    private val behandlingService: BehandlingService,
    private val fagsakRepository: FagsakRepository,
    private val fagsakPersonService: FagsakPersonService,
    private val brevService: BrevService,
    private val dokarkivClient: DokarkivClient,
) : AsyncTaskStep {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val behandlingId = UUID.fromString(task.payload)
        val behandling =
            behandlingService.hentBehandling(behandlingId)
                ?: error("Fant ikke behandling med id=$behandlingId")
        val fagsak =
            fagsakRepository.findById(behandling.fagsakId).orElseThrow {
                error("Fant ikke fagsak med id=${behandling.fagsakId}")
            }
        val personident = fagsakPersonService.hentAktivIdent(fagsak.fagsakPersonId)
        val vedtaksbrev =
            brevService.hentBrev(behandlingId)
                ?: error("Fant ikke brev for behandlingId=$behandlingId")
        val brevPdf = vedtaksbrev.brevPdf ?: error("Vedtaksbrev mangler PDF for behandlingId=$behandlingId")
        val dokument =
            Dokument(
                dokument = brevPdf,
                filtype = Filtype.PDFA,
                dokumenttype = Dokumenttype.BARNETILSYNSTØNAD_VEDTAK, // TODO utlede
                tittel = "Test-tittel", // TODO ikke dette
            )
        val dokarkivResponse =
            dokarkivClient.arkiverDokument(
                ArkiverDokumentRequest(
                    fnr = personident, // TODO Er dette alltid ett fnr? Er det riktig å bruke personident?
                    `forsøkFerdigstill` = true,
                    hoveddokumentvarianter = listOf(dokument),
                    vedleggsdokumenter = emptyList(), // TODO
                    fagsakId = fagsak.eksternId.toString(),
                    `journalførendeEnhet` = "", // TODO
//                `førsteside` = TODO,
                    eksternReferanseId = "$behandlingId-vedtaksbrev",
//                avsenderMottaker = "", TODO
                ),
            )
        // TODO lagre journalpostId og dokumentId fra dokarkivResponse i iverksettResultat
    }

    companion object {
        const val TYPE = "journalførVedtaksbrev"

        fun opprettTask(payload: String): Task =
            Task(
                TYPE,
                payload,
            )
    }

    data class JournalførVedtaksbrevTaskData(
        val behandlingId: UUID,
    )
}
