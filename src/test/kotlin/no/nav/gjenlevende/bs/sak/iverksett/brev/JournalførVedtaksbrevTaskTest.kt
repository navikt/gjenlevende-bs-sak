package no.nav.gjenlevende.bs.sak.iverksett.brev

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.prosessering.domene.Task
import no.nav.gjenlevende.bs.sak.behandling.BehandlingService
import no.nav.gjenlevende.bs.sak.brev.BrevService
import no.nav.gjenlevende.bs.sak.brev.BrevmottakerService
import no.nav.gjenlevende.bs.sak.fagsak.FagsakPersonService
import no.nav.gjenlevende.bs.sak.fagsak.FagsakRepository
import no.nav.gjenlevende.bs.sak.iverksett.DokarkivClient
import no.nav.gjenlevende.bs.sak.iverksett.domene.AvsenderMottaker
import no.nav.gjenlevende.bs.sak.iverksett.domene.AvsenderMottakerIdType
import no.nav.gjenlevende.bs.sak.iverksett.domene.JournalpostRequest
import no.nav.gjenlevende.bs.sak.iverksett.domene.JournalpostType
import org.assertj.core.api.Assertions.assertThat
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.UUID
import kotlin.test.Test

class JournalførVedtaksbrevTaskTest {
    private val behandlingService = mockk<BehandlingService>()
    private val fagsakRepository = mockk<FagsakRepository>()
    private val fagsakPersonService = mockk<FagsakPersonService>()
    private val brevService = mockk<BrevService>()
    private val brevmottakerService = mockk<BrevmottakerService>()
    private val dokarkivClient = mockk<DokarkivClient>()
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    private val journalføringService = mockk<JournalføringService>()
    private val journalførVedtaksbrevTask =
        JournalførVedtaksbrevTask(
            behandlingService = behandlingService,
            fagsakRepository = fagsakRepository,
            fagsakPersonService = fagsakPersonService,
            brevService = brevService,
            brevmottakerService = brevmottakerService,
            dokarkivClient = dokarkivClient,
            objectMapper = objectMapper,
            journalføringService = journalføringService,
        )

    @Test
    fun `doTask skal journalføre vedtaksbrev for én mottaker`() {
        val behandlingId = UUID.randomUUID()
        val taskData = JournalførVedtaksbrevTask.JournalførVedtaksbrevTaskData(behandlingId)
        val payload = objectMapper.writeValueAsString(taskData)
        val task = Task(JournalførVedtaksbrevTask.TYPE, payload)
        val journalpostRequest = lagJournalpostRequest(behandlingId, 0)

        every { journalføringService.lagJournalføringRequester(behandlingId) } returns listOf(journalpostRequest)
        every { dokarkivClient.arkiverDokument(journalpostRequest) } returns """{"journalpostId": "123456"}"""

        journalførVedtaksbrevTask.doTask(task)

        verify(exactly = 1) { journalføringService.lagJournalføringRequester(behandlingId) }
        verify(exactly = 1) { dokarkivClient.arkiverDokument(journalpostRequest) }
    }

    @Test
    fun `doTask skal journalføre vedtaksbrev for flere mottakere`() {
        val behandlingId = UUID.randomUUID()
        val taskData = JournalførVedtaksbrevTask.JournalførVedtaksbrevTaskData(behandlingId)
        val payload = objectMapper.writeValueAsString(taskData)
        val task = Task(JournalførVedtaksbrevTask.TYPE, payload)
        val request1 = lagJournalpostRequest(behandlingId, 0)
        val request2 = lagJournalpostRequest(behandlingId, 1)

        every { journalføringService.lagJournalføringRequester(behandlingId) } returns listOf(request1, request2)
        every { dokarkivClient.arkiverDokument(request1) } returns """{"journalpostId": "123456"}"""
        every { dokarkivClient.arkiverDokument(request2) } returns """{"journalpostId": "123457"}"""

        journalførVedtaksbrevTask.doTask(task)

        verify(exactly = 1) { journalføringService.lagJournalføringRequester(behandlingId) }
        verify(exactly = 1) { dokarkivClient.arkiverDokument(request1) }
        verify(exactly = 1) { dokarkivClient.arkiverDokument(request2) }
    }

    @Test
    fun `opprettTask skal opprette task med riktig type`() {
        val payload = """{"behandlingId": "123e4567-e89b-12d3-a456-426614174000"}"""
        val task = JournalførVedtaksbrevTask.opprettTask(payload)

        assertThat(task.type).isEqualTo(JournalførVedtaksbrevTask.TYPE)
        assertThat(task.payload).isEqualTo(payload)
    }

    @Test
    fun `TYPE konstant skal være korrekt`() {
        assertThat(JournalførVedtaksbrevTask.TYPE).isEqualTo("journalførVedtaksbrev")
    }

    private fun lagJournalpostRequest(
        behandlingId: UUID,
        mottakerIndeks: Int,
    ) = JournalpostRequest(
        journalpostType = JournalpostType.UTGAAENDE,
        avsenderMottaker =
            AvsenderMottaker(
                id = "12345678901",
                idType = AvsenderMottakerIdType.FNR,
                navn = "Test Person",
            ),
        tema = "EYO",
        tittel = "Vedtak om innvilget stønad til barnetilsyn",
        eksternReferanseId = "$behandlingId-vedtaksbrev-mottaker$mottakerIndeks",
    )
}
