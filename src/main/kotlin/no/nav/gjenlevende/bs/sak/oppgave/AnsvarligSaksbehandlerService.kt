package no.nav.gjenlevende.bs.sak.oppgave

import no.nav.gjenlevende.bs.sak.behandling.BehandlingRepository
import no.nav.gjenlevende.bs.sak.felles.sikkerhet.SikkerhetContext
import no.nav.gjenlevende.bs.sak.oppgave.dto.AnsvarligSaksbehandlerDto
import no.nav.gjenlevende.bs.sak.oppgave.dto.SaksbehandlerRolle
import no.nav.gjenlevende.bs.sak.saksbehandler.EntraProxyClient
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AnsvarligSaksbehandlerService(
    private val oppgaveRepository: OppgaveRepository,
    private val oppgaveClient: OppgaveClient,
    private val entraProxyClient: EntraProxyClient,
    private val behandlingRepository: BehandlingRepository,
) {
    private val logger = LoggerFactory.getLogger(AnsvarligSaksbehandlerService::class.java)

    fun hentAnsvarligSaksbehandler(behandlingId: UUID): AnsvarligSaksbehandlerDto {
        val innloggetSaksbehandler = SikkerhetContext.hentSaksbehandler()

        val oppgave =
            oppgaveRepository.findByBehandlingIdAndType(
                behandlingId = behandlingId,
                type = OppgavetypeEYO.BEH_SAK.name,
            )

        val ansvarligSaksbehandler =
            if (oppgave != null) {
                val gosysOppgave = oppgaveClient.hentOppgaveM2M(oppgave.gsakOppgaveId)
                gosysOppgave.tilordnetRessurs
            } else {
                logger.info("Ingen oppgave funnet for behandling=$behandlingId, bruker opprettetAv fra behandling")
                val behandling =
                    behandlingRepository.findByIdOrNull(behandlingId)
                        ?: throw IllegalStateException("Finner ikke behandling med id=$behandlingId")
                behandling.sporing.opprettetAv
            }

        if (ansvarligSaksbehandler.isNullOrBlank()) {
            return AnsvarligSaksbehandlerDto(
                fornavn = "",
                etternavn = "",
                rolle = SaksbehandlerRolle.IKKE_SATT,
            )
        }

        val saksbehandlerInfo = entraProxyClient.hentSaksbehandlerInfo(ansvarligSaksbehandler)
        val rolle =
            if (ansvarligSaksbehandler == innloggetSaksbehandler) {
                SaksbehandlerRolle.INNLOGGET_SAKSBEHANDLER
            } else {
                SaksbehandlerRolle.ANNEN_SAKSBEHANDLER
            }

        return AnsvarligSaksbehandlerDto(
            fornavn = saksbehandlerInfo.fornavn,
            etternavn = saksbehandlerInfo.etternavn,
            rolle = rolle,
        )
    }
}
