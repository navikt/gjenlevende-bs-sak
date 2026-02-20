package no.nav.gjenlevende.bs.sak.iverksett.brev

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.gjenlevende.bs.sak.behandling.BehandlingService
import no.nav.gjenlevende.bs.sak.brev.BrevService
import no.nav.gjenlevende.bs.sak.brev.BrevmottakerService
import no.nav.gjenlevende.bs.sak.brev.domain.Brevmottaker
import no.nav.gjenlevende.bs.sak.brev.domain.MottakerType
import no.nav.gjenlevende.bs.sak.fagsak.FagsakPersonService
import no.nav.gjenlevende.bs.sak.fagsak.FagsakRepository
import no.nav.gjenlevende.bs.sak.iverksett.DokarkivClient
import no.nav.gjenlevende.bs.sak.iverksett.domene.ArkiverDokumentRequest
import no.nav.gjenlevende.bs.sak.iverksett.domene.AvsenderMottaker
import no.nav.gjenlevende.bs.sak.iverksett.domene.AvsenderMottakerIdType
import no.nav.gjenlevende.bs.sak.iverksett.domene.Dokument
import no.nav.gjenlevende.bs.sak.iverksett.domene.Dokumenttype
import no.nav.gjenlevende.bs.sak.iverksett.domene.Filtype
import no.nav.gjenlevende.bs.sak.saf.Arkivtema
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
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
    private val brevmottakerService: BrevmottakerService,
    private val dokarkivClient: DokarkivClient,
    private val objectMapper: ObjectMapper,
) : AsyncTaskStep {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val taskData = objectMapper.readValue<JournalførVedtaksbrevTaskData>(task.payload)
        val behandlingId = taskData.behandlingId
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
        val saksbehandlerEnhet = "" // TODO
        val mottakere = brevmottakerService.hentBrevmottakere(behandlingId)
        require(mottakere.isNotEmpty()) { "Ingen brevmottakere funnet for behandlingId=$behandlingId" }
        mottakere.forEachIndexed { indeks, mottaker ->
            val arkiverDokumentRequestForMottaker =
                ArkiverDokumentRequest(
                    fnr = personident, // TODO Er dette alltid ett fnr? Er det riktig å bruke personident?
                    forsøkFerdigstill = true,
                    tema = Arkivtema.EYO, // TODO utlede tema
                    hoveddokumentvarianter = listOf(dokument),
                    vedleggsdokumenter = emptyList(), // TODO vedlegg
                    fagsakId = fagsak.eksternId.toString(),
                    journalførendeEnhet = saksbehandlerEnhet,
                    eksternReferanseId = "$behandlingId-vedtaksbrev-mottaker$indeks",
                    avsenderMottaker = mottaker.tilAvsenderMottaker(),
                )
            val response = dokarkivClient.arkiverDokument(arkiverDokumentRequestForMottaker)
            logger.info("Journalført vedtaksbrev for mottaker ${mottaker.id}: $response")
            // TODO lagre journalpostId og dokumentId fra dokarkivResponse i iverksettResultat
        }
    }

    private fun Brevmottaker.tilAvsenderMottaker(): AvsenderMottaker =
        AvsenderMottaker(
            id =
                when (mottakerType) {
                    MottakerType.PERSON -> personident
                    MottakerType.ORGANISASJON -> orgnr
                },
            idType =
                when (mottakerType) {
                    MottakerType.PERSON -> AvsenderMottakerIdType.FNR
                    MottakerType.ORGANISASJON -> AvsenderMottakerIdType.ORGNR
                },
            navn =
                when (mottakerType) {
                    MottakerType.PERSON -> ""

                    // TODO hent navn for personident
                    MottakerType.ORGANISASJON -> navnHosOrganisasjon ?: ""
                },
        )

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
