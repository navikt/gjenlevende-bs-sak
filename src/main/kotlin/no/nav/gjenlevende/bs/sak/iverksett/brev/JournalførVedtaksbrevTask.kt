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
import no.nav.gjenlevende.bs.sak.fagsak.domain.StønadType
import no.nav.gjenlevende.bs.sak.iverksett.DokarkivClient
import no.nav.gjenlevende.bs.sak.iverksett.domene.ArkivDokument
import no.nav.gjenlevende.bs.sak.iverksett.domene.AvsenderMottaker
import no.nav.gjenlevende.bs.sak.iverksett.domene.AvsenderMottakerIdType
import no.nav.gjenlevende.bs.sak.iverksett.domene.DokarkivBruker
import no.nav.gjenlevende.bs.sak.iverksett.domene.Dokument
import no.nav.gjenlevende.bs.sak.iverksett.domene.Dokumentkategori
import no.nav.gjenlevende.bs.sak.iverksett.domene.Dokumenttype
import no.nav.gjenlevende.bs.sak.iverksett.domene.Dokumentvariant
import no.nav.gjenlevende.bs.sak.iverksett.domene.Fagsystem
import no.nav.gjenlevende.bs.sak.iverksett.domene.Filtype
import no.nav.gjenlevende.bs.sak.iverksett.domene.JournalpostRequest
import no.nav.gjenlevende.bs.sak.iverksett.domene.JournalpostType
import no.nav.gjenlevende.bs.sak.iverksett.domene.Sak
import no.nav.gjenlevende.bs.sak.iverksett.metadata.tilMetadata
import no.nav.gjenlevende.bs.sak.saf.BrukerIdType
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
                dokumenttype = vedtaksbrevForStønadType(fagsak.stønadstype),
                tittel = "Test-tittel", // TODO ikke dette
            )
        val metadata = dokument.dokumenttype.tilMetadata()
        val saksbehandlerEnhet = "4489" // TODO må hente fra db, etter å ha henta fra register
        val mottakere = brevmottakerService.hentBrevmottakere(behandlingId)
        val dokarkivBruker = DokarkivBruker(BrukerIdType.FNR, personident)
        val sak =
            Sak(fagsakId = fagsak.eksternId.toString(), sakstype = "FAGSAK", fagsaksystem = Fagsystem.EY)
        val dokumenter =
            listOf(
                ArkivDokument(
                    tittel = "", // TODO
                    brevkode = metadata.brevkode,
                    dokumentKategori = metadata.dokumentKategori,
                    dokumentvarianter =
                        listOf(
                            Dokumentvariant(
                                // TODO placeholder
                                filtype = "PDFA",
                                variantformat = "ARKIV",
                                fysiskDokument = brevPdf,
                                filnavn = "vedtaksbrev.pdf",
                            ),
                        ),
                ),
            )

        require(mottakere.isNotEmpty()) { "Ingen brevmottakere funnet for behandlingId=$behandlingId" }
        mottakere.forEachIndexed { indeks, mottaker ->
            val journalpostRequest =
                JournalpostRequest(
                    journalpostType = metadata.journalpostType,
                    behandlingstema = metadata.behandlingstema?.value,
                    avsenderMottaker = mottaker.tilAvsenderMottaker(),
                    bruker = dokarkivBruker,
                    tema = metadata.tema.navn,
                    tittel = dokument.tittel ?: metadata.tittel,
                    kanal = metadata.kanal,
                    journalfoerendeEnhet = saksbehandlerEnhet,
                    eksternReferanseId = "$behandlingId-vedtaksbrev-mottaker$indeks", // TODO må være unik for hver mottaker, legg til indeks
                    sak = sak,
                    dokumenter = dokumenter,
                )
            val response = dokarkivClient.arkiverDokument(journalpostRequest)
            logger.info("Journalført vedtaksbrev for mottaker ${mottaker.id}: $response")
        }
        // TODO lagre journalpostId og dokumentId fra dokarkivResponse i iverksettResultat
//        }
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

    fun vedtaksbrevForStønadType(stønadType: StønadType): Dokumenttype =
        when (stønadType) {
            StønadType.BARNETILSYN -> Dokumenttype.VEDTAKSBREV_BARNETILSYN
            StønadType.SKOLEPENGER -> Dokumenttype.VEDTAKSBREV_SKOLEPENGER
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
