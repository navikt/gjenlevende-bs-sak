package no.nav.gjenlevende.bs.sak.felles

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.gjenlevende.bs.sak.SpringContextTest
import no.nav.gjenlevende.bs.sak.behandling.Behandling
import no.nav.gjenlevende.bs.sak.behandling.BehandlingRepository
import no.nav.gjenlevende.bs.sak.behandling.BehandlingResultat
import no.nav.gjenlevende.bs.sak.behandling.BehandlingStatus
import no.nav.gjenlevende.bs.sak.fagsak.FagsakPersonRepository
import no.nav.gjenlevende.bs.sak.fagsak.FagsakRepository
import no.nav.gjenlevende.bs.sak.fagsak.domain.Fagsak
import no.nav.gjenlevende.bs.sak.fagsak.domain.FagsakPerson
import no.nav.gjenlevende.bs.sak.fagsak.domain.Personident
import no.nav.gjenlevende.bs.sak.fagsak.domain.StønadType
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.SikkerhetContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime

class SporbarAuditingTest : SpringContextTest() {
    @Autowired
    private lateinit var behandlingRepository: BehandlingRepository

    @Autowired
    private lateinit var fagsakRepository: FagsakRepository

    @Autowired
    private lateinit var fagsakPersonRepository: FagsakPersonRepository

    @BeforeEach
    fun setUp() {
        mockkObject(SikkerhetContext)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(SikkerhetContext)
    }

    @Test
    fun `insert setter sporbar felter med riktige verdier`() {
        every { SikkerhetContext.hentSaksbehandlerEllerSystembruker() } returns BRUKER_A

        val behandling = opprettBehandling()

        val lagretBehandling = checkNotNull(behandlingRepository.findByIdOrNull(behandling.id))

        assertThat(lagretBehandling.sporbar.opprettetAv).isEqualTo(BRUKER_A)
        assertThat(lagretBehandling.sporbar.opprettetTid).isBeforeOrEqualTo(LocalDateTime.now())
        assertThat(lagretBehandling.sporbar.endret.endretAv).isEqualTo(BRUKER_A)
        assertThat(lagretBehandling.sporbar.endret.endretTid).isBeforeOrEqualTo(LocalDateTime.now())
    }

    @Test
    fun `update oppdaterer endretAv og endretTid, beholder opprettetAv og opprettetTid`() {
        every { SikkerhetContext.hentSaksbehandlerEllerSystembruker() } returns BRUKER_A

        val behandling = opprettBehandling()
        val lagretBehandling = checkNotNull(behandlingRepository.findByIdOrNull(behandling.id))
        val opprinneligOpprettetAv = lagretBehandling.sporbar.opprettetAv
        val opprinneligOpprettetTid = lagretBehandling.sporbar.opprettetTid

        Thread.sleep(10)

        every { SikkerhetContext.hentSaksbehandlerEllerSystembruker() } returns BRUKER_B

        behandlingRepository.update(lagretBehandling.copy(status = BehandlingStatus.UTREDES))

        val oppdatertBehandling = checkNotNull(behandlingRepository.findByIdOrNull(behandling.id))

        assertThat(oppdatertBehandling.sporbar.opprettetAv).isEqualTo(opprinneligOpprettetAv)
        assertThat(oppdatertBehandling.sporbar.opprettetTid).isEqualTo(opprinneligOpprettetTid)
        assertThat(oppdatertBehandling.sporbar.endret.endretAv).isEqualTo(BRUKER_B)
        assertThat(oppdatertBehandling.sporbar.endret.endretTid).isAfter(opprinneligOpprettetTid)
    }

    private fun opprettBehandling(): Behandling {
        val fagsakPerson =
            fagsakPersonRepository.insert(
                FagsakPerson(identer = setOf(Personident("01010199999"))),
            )
        val fagsak =
            fagsakRepository.insert(
                Fagsak(fagsakPersonId = fagsakPerson.id, stønadstype = StønadType.BARNETILSYN),
            )
        return behandlingRepository.insert(
            Behandling(
                fagsakId = fagsak.id,
                status = BehandlingStatus.OPPRETTET,
                resultat = BehandlingResultat.IKKE_SATT,
            ),
        )
    }

    companion object {
        private const val BRUKER_A = "A100000"
        private const val BRUKER_B = "B200000"
    }
}
