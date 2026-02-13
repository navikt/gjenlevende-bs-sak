package no.nav.gjenlevende.bs.sak.oppgave

import EnhetResponse
import SaksbehandlerResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.gjenlevende.bs.sak.behandling.Behandling
import no.nav.gjenlevende.bs.sak.behandling.BehandlingRepository
import no.nav.gjenlevende.bs.sak.behandling.BehandlingResultat
import no.nav.gjenlevende.bs.sak.behandling.BehandlingStatus
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.SikkerhetContext
import no.nav.gjenlevende.bs.sak.felles.sporbar.Sporing
import no.nav.gjenlevende.bs.sak.oppgave.dto.SaksbehandlerRolle
import no.nav.gjenlevende.bs.sak.saksbehandler.EntraProxyClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class AnsvarligSaksbehandlerServiceTest {
    private val oppgaveRepository = mockk<OppgaveRepository>()
    private val oppgaveClient = mockk<OppgaveClient>()
    private val entraProxyClient = mockk<EntraProxyClient>()
    private val behandlingRepository = mockk<BehandlingRepository>()

    private val service =
        AnsvarligSaksbehandlerService(
            oppgaveRepository = oppgaveRepository,
            oppgaveClient = oppgaveClient,
            entraProxyClient = entraProxyClient,
            behandlingRepository = behandlingRepository,
        )

    @BeforeEach
    fun setUp() {
        mockkObject(SikkerhetContext)
        every { SikkerhetContext.hentSaksbehandler() } returns INNLOGGET_IDENT
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(SikkerhetContext)
    }

    @Test
    fun `returnerer INNLOGGET_SAKSBEHANDLER når tilordnetRessurs er det samme som innlogget bruker`() {
        val behandlingId = UUID.randomUUID()
        val gsakOppgaveId = 12345L

        every {
            oppgaveRepository.findByBehandlingIdAndType(
                behandlingId = behandlingId,
                type = OppgavetypeEYO.BEH_SAK.name,
            )
        } returns lagOppgaveEntity(behandlingId = behandlingId, gsakOppgaveId = gsakOppgaveId)

        every { oppgaveClient.hentOppgaveM2M(gsakOppgaveId) } returns lagGosysOppgave(tilordnetRessurs = INNLOGGET_IDENT)
        every { entraProxyClient.hentSaksbehandlerInfo(INNLOGGET_IDENT) } returns lagSaksbehandlerResponse(navIdent = INNLOGGET_IDENT)

        val resultat = service.hentAnsvarligSaksbehandler(behandlingId)

        assertThat(resultat.rolle).isEqualTo(SaksbehandlerRolle.INNLOGGET_SAKSBEHANDLER)
        assertThat(resultat.fornavn).isEqualTo("Fornavn")
        assertThat(resultat.etternavn).isEqualTo("Etternavn")
    }

    @Test
    fun `returnerer ANNEN_SAKSBEHANDLER når tilordnetRessurs er en annen bruker`() {
        val behandlingId = UUID.randomUUID()
        val gsakOppgaveId = 12345L
        val annenIdent = "B654321"

        every {
            oppgaveRepository.findByBehandlingIdAndType(
                behandlingId = behandlingId,
                type = OppgavetypeEYO.BEH_SAK.name,
            )
        } returns lagOppgaveEntity(behandlingId = behandlingId, gsakOppgaveId = gsakOppgaveId)

        every { oppgaveClient.hentOppgaveM2M(gsakOppgaveId) } returns lagGosysOppgave(tilordnetRessurs = annenIdent)
        every { entraProxyClient.hentSaksbehandlerInfo(annenIdent) } returns lagSaksbehandlerResponse(navIdent = annenIdent)

        val resultat = service.hentAnsvarligSaksbehandler(behandlingId)

        assertThat(resultat.rolle).isEqualTo(SaksbehandlerRolle.ANNEN_SAKSBEHANDLER)
    }

    @Test
    fun `returnerer IKKE_SATT når tilordnetRessurs er null`() {
        val behandlingId = UUID.randomUUID()
        val gsakOppgaveId = 12345L

        every {
            oppgaveRepository.findByBehandlingIdAndType(
                behandlingId = behandlingId,
                type = OppgavetypeEYO.BEH_SAK.name,
            )
        } returns lagOppgaveEntity(behandlingId = behandlingId, gsakOppgaveId = gsakOppgaveId)

        every { oppgaveClient.hentOppgaveM2M(gsakOppgaveId) } returns lagGosysOppgave(tilordnetRessurs = null)

        val resultat = service.hentAnsvarligSaksbehandler(behandlingId)

        assertThat(resultat.rolle).isEqualTo(SaksbehandlerRolle.IKKE_SATT)
        assertThat(resultat.fornavn).isEmpty()
        assertThat(resultat.etternavn).isEmpty()
    }

    @Test
    fun `bruker opprettetAv når oppgaveEntity ikke finnes og opprettetAv er det samme som innlogget bruker`() {
        val behandlingId = UUID.randomUUID()

        every {
            oppgaveRepository.findByBehandlingIdAndType(
                behandlingId = behandlingId,
                type = OppgavetypeEYO.BEH_SAK.name,
            )
        } returns null

        every { behandlingRepository.findByIdOrNull(behandlingId) } returns
            lagBehandling(
                id = behandlingId,
                opprettetAv = INNLOGGET_IDENT,
            )
        every { entraProxyClient.hentSaksbehandlerInfo(INNLOGGET_IDENT) } returns lagSaksbehandlerResponse(navIdent = INNLOGGET_IDENT)

        val resultat = service.hentAnsvarligSaksbehandler(behandlingId)

        assertThat(resultat.rolle).isEqualTo(SaksbehandlerRolle.INNLOGGET_SAKSBEHANDLER)
    }

    @Test
    fun `bruker opprettetAv når oppgaveEntity ikke finnes og opprettetAv er annen bruker`() {
        val behandlingId = UUID.randomUUID()
        val annenIdent = "B654321"

        every {
            oppgaveRepository.findByBehandlingIdAndType(
                behandlingId = behandlingId,
                type = OppgavetypeEYO.BEH_SAK.name,
            )
        } returns null

        every { behandlingRepository.findByIdOrNull(behandlingId) } returns
            lagBehandling(
                id = behandlingId,
                opprettetAv = annenIdent,
            )
        every { entraProxyClient.hentSaksbehandlerInfo(annenIdent) } returns lagSaksbehandlerResponse(navIdent = annenIdent)

        val resultat = service.hentAnsvarligSaksbehandler(behandlingId)

        assertThat(resultat.rolle).isEqualTo(SaksbehandlerRolle.ANNEN_SAKSBEHANDLER)
    }

    companion object {
        private const val INNLOGGET_IDENT = "A123456"

        private fun lagOppgaveEntity(
            behandlingId: UUID,
            gsakOppgaveId: Long,
        ) = Oppgave(
            behandlingId = behandlingId,
            gsakOppgaveId = gsakOppgaveId,
            type = OppgavetypeEYO.BEH_SAK.name,
        )

        private fun lagGosysOppgave(tilordnetRessurs: String?) =
            OppgaveDto(
                id = 12345L,
                tema = Tema.EYO,
                oppgavetype = OppgavetypeEYO.BEH_SAK.name,
                tilordnetRessurs = tilordnetRessurs,
            )

        private fun lagSaksbehandlerResponse(navIdent: String) =
            SaksbehandlerResponse(
                navIdent = navIdent,
                visningNavn = "Fornavn Etternavn",
                fornavn = "Fornavn",
                etternavn = "Etternavn",
                tIdent = navIdent,
                epost = "fornavn.etternavn@nav.no",
                enhet = EnhetResponse(enhetnummer = "4415", navn = "NAV Molde"),
            )

        private fun lagBehandling(
            id: UUID,
            opprettetAv: String,
        ) = Behandling(
            id = id,
            fagsakId = UUID.randomUUID(),
            status = BehandlingStatus.OPPRETTET,
            resultat = BehandlingResultat.IKKE_SATT,
            sporing = Sporing(opprettetAv = opprettetAv),
        )
    }
}
