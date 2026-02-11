package no.nav.gjenlevende.bs.sak.oppgave

import no.nav.gjenlevende.bs.sak.felles.sikkerhet.SikkerhetContext
import no.nav.gjenlevende.bs.sak.oppgave.dto.AnsvarligSaksbehandlerDto
import no.nav.gjenlevende.bs.sak.oppgave.dto.SaksbehandlerRolle
import no.nav.gjenlevende.bs.sak.saksbehandler.EntraProxyClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TilordnetRessursService(
    private val oppgaveRepository: OppgaveRepository,
    private val oppgaveClient: OppgaveClient,
    private val entraProxyClient: EntraProxyClient,
) {
    private val logger = LoggerFactory.getLogger(TilordnetRessursService::class.java)

    fun hentAnsvarligSaksbehandler(behandlingId: UUID): AnsvarligSaksbehandlerDto {
        val innloggetSaksbehandler = SikkerhetContext.hentSaksbehandler()

        val oppgaveEntity =
            oppgaveRepository.findByBehandlingIdAndType(
                behandlingId = behandlingId,
                type = OppgavetypeEYO.BEH_SAK.name,
            )

        if (oppgaveEntity == null) {
            logger.info("Ingen oppgave funnet for behandling=$behandlingId, antar innlogget bruker er eier")
            val saksbehandlerInfo = entraProxyClient.hentSaksbehandlerInfo(innloggetSaksbehandler)
            return AnsvarligSaksbehandlerDto(
                fornavn = saksbehandlerInfo.fornavn,
                etternavn = saksbehandlerInfo.etternavn,
                rolle = SaksbehandlerRolle.INNLOGGET_SAKSBEHANDLER,
            )
        }

        val gosysOppgave = oppgaveClient.hentOppgaveM2M(oppgaveEntity.gsakOppgaveId)
        val tilordnetRessurs = gosysOppgave.tilordnetRessurs

        if (tilordnetRessurs.isNullOrBlank()) {
            return AnsvarligSaksbehandlerDto(
                fornavn = "",
                etternavn = "",
                rolle = SaksbehandlerRolle.IKKE_SATT,
            )
        }

        val saksbehandlerInfo = entraProxyClient.hentSaksbehandlerInfo(tilordnetRessurs)
        val rolle =
            if (tilordnetRessurs == innloggetSaksbehandler) {
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
