package no.nav.gjenlevende.bs.sak.felles

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import no.nav.gjenlevende.bs.sak.behandling.Behandling
import no.nav.gjenlevende.bs.sak.behandling.BehandlingResultat
import no.nav.gjenlevende.bs.sak.behandling.BehandlingStatus
import no.nav.gjenlevende.bs.sak.behandling.årsak.Årsak
import no.nav.gjenlevende.bs.sak.behandling.årsak.ÅrsakBehandling
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.SikkerhetContext
import no.nav.gjenlevende.bs.sak.felles.sporbar.Sporing
import no.nav.gjenlevende.bs.sak.vilkår.VilkårType
import no.nav.gjenlevende.bs.sak.vilkår.VilkårVurdering
import no.nav.gjenlevende.bs.sak.vilkår.Vurdering
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.jdbc.core.JdbcAggregateOperations
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class InsertUpdateRepositoryImplTest {
    private val entityOperations = mockk<JdbcAggregateOperations>()
    private val repository = InsertUpdateRepositoryImpl<Any>(entityOperations)

    private val opprinneligTidspunkt = LocalDateTime.of(2026, 1, 1, 12, 0, 0)
    private val opprinneligBruker = "ORIGINAL_BRUKER"
    private val aktivBruker = "Z123456"

    @BeforeEach
    fun setUp() {
        mockkObject(SikkerhetContext)
        every { SikkerhetContext.hentSaksbehandlerEllerSystembruker() } returns aktivBruker
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(SikkerhetContext)
    }

    @Test
    fun `update på Behandling oppdaterer endretAv og endretTid, men beholder opprettetAv og opprettetTid`() {
        val lagretEntity = slot<Behandling>()
        every { entityOperations.update(capture(lagretEntity)) } answers { lagretEntity.captured }

        val behandling = lagBehandling()

        repository.update(behandling)

        val lagret = lagretEntity.captured
        assertThat(lagret.sporing.opprettetAv).isEqualTo(opprinneligBruker)
        assertThat(lagret.sporing.opprettetTid).isEqualTo(opprinneligTidspunkt)
        assertThat(lagret.sporing.endretAv).isEqualTo(aktivBruker)
        assertThat(lagret.sporing.endretTid).isAfter(opprinneligTidspunkt)
    }

    @Test
    fun `update på VilkårVurdering oppdaterer endretAv og endretTid`() {
        val lagretEntity = slot<VilkårVurdering>()
        every { entityOperations.update(capture(lagretEntity)) } answers { lagretEntity.captured }

        val vilkår = lagVilkårVurdering()

        repository.update(vilkår)

        val lagret = lagretEntity.captured
        assertThat(lagret.sporing.opprettetAv).isEqualTo(opprinneligBruker)
        assertThat(lagret.sporing.opprettetTid).isEqualTo(opprinneligTidspunkt)
        assertThat(lagret.sporing.endretAv).isEqualTo(aktivBruker)
        assertThat(lagret.sporing.endretTid).isAfter(opprinneligTidspunkt)
    }

    @Test
    fun `update på entitet uten sporing passerer uendret gjennom`() {
        val lagretEntity = slot<ÅrsakBehandling>()
        every { entityOperations.update(capture(lagretEntity)) } answers { lagretEntity.captured }

        val årsak = lagÅrsakBehandling()

        repository.update(årsak)

        assertThat(lagretEntity.captured).isEqualTo(årsak)
    }

    @Test
    fun `insert beholder opprinnelig sporing uendret`() {
        val lagretEntity = slot<Behandling>()
        every { entityOperations.insert(capture(lagretEntity)) } answers { lagretEntity.captured }

        val behandling = lagBehandling()

        repository.insert(behandling)

        val lagret = lagretEntity.captured
        assertThat(lagret.sporing.opprettetAv).isEqualTo(opprinneligBruker)
        assertThat(lagret.sporing.opprettetTid).isEqualTo(opprinneligTidspunkt)
        assertThat(lagret.sporing.endretAv).isEqualTo(opprinneligBruker)
        assertThat(lagret.sporing.endretTid).isEqualTo(opprinneligTidspunkt)
    }

    @Test
    fun `update endrer ikke andre felt på entiteten`() {
        val lagretEntity = slot<Behandling>()
        every { entityOperations.update(capture(lagretEntity)) } answers { lagretEntity.captured }

        val behandling = lagBehandling()

        repository.update(behandling)

        val lagret = lagretEntity.captured
        assertThat(lagret.id).isEqualTo(behandling.id)
        assertThat(lagret.fagsakId).isEqualTo(behandling.fagsakId)
        assertThat(lagret.status).isEqualTo(behandling.status)
        assertThat(lagret.resultat).isEqualTo(behandling.resultat)
    }

    private fun lagBehandling() =
        Behandling(
            fagsakId = UUID.randomUUID(),
            status = BehandlingStatus.UTREDES,
            resultat = BehandlingResultat.IKKE_SATT,
            sporing =
                Sporing(
                    opprettetAv = opprinneligBruker,
                    opprettetTid = opprinneligTidspunkt,
                    endretAv = opprinneligBruker,
                    endretTid = opprinneligTidspunkt,
                ),
        )

    private fun lagVilkårVurdering() =
        VilkårVurdering(
            behandlingId = UUID.randomUUID(),
            vilkårType = VilkårType.INNGANGSVILKÅR,
            vurdering = Vurdering.JA,
            sporing =
                Sporing(
                    opprettetAv = opprinneligBruker,
                    opprettetTid = opprinneligTidspunkt,
                    endretAv = opprinneligBruker,
                    endretTid = opprinneligTidspunkt,
                ),
        )

    private fun lagÅrsakBehandling() =
        ÅrsakBehandling(
            behandlingId = UUID.randomUUID(),
            kravdato = LocalDate.of(2026, 1, 1),
            årsak = Årsak.SØKNAD,
        )
}
