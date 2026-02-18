package no.nav.gjenlevende.bs.sak.beslutter

import no.nav.familie.prosessering.internal.TaskService
import no.nav.gjenlevende.bs.sak.behandling.BehandlingResultat
import no.nav.gjenlevende.bs.sak.behandling.BehandlingService
import no.nav.gjenlevende.bs.sak.behandling.BehandlingStatus
import no.nav.gjenlevende.bs.sak.beslutter.dto.BeslutteVedtakDto
import no.nav.gjenlevende.bs.sak.brev.BrevService
import no.nav.gjenlevende.bs.sak.endringshistorikk.EndringType
import no.nav.gjenlevende.bs.sak.endringshistorikk.EndringshistorikkService
import no.nav.gjenlevende.bs.sak.oppgave.OppgaveService
import no.nav.gjenlevende.bs.sak.oppgave.OppgavetypeEYO
import no.nav.gjenlevende.bs.sak.task.FerdigstillOppgaveTask
import no.nav.gjenlevende.bs.sak.task.OpprettOppgaveTask
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@Service
class BeslutterService(
    private val behandlingService: BehandlingService,
    private val brevService: BrevService,
    private val endringshistorikkService: EndringshistorikkService,
    private val oppgaveService: OppgaveService,
    private val totrinnskontrollService: TotrinnskontrollService,
    private val taskService: TaskService,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun sendTilBeslutter(behandlingId: UUID) {
        brevService.oppdaterSaksbehandlerForBrev(behandlingId)
        behandlingService.oppdaterBehandlingStatus(
            behandlingId = behandlingId,
            status = BehandlingStatus.FATTER_VEDTAK,
        )
        endringshistorikkService.registrerEndring(
            behandlingId = behandlingId,
            endringType = EndringType.SENDT_TIL_BESLUTTER,
        )

        val aktivOppgavetype = oppgaveService.hentAktivOppgavetype(behandlingId)

        FerdigstillOppgaveTask.opprettTask(
            behandlingId = behandlingId,
            oppgavetype = aktivOppgavetype,
            objectMapper = objectMapper,
            taskService = taskService,
        )

        OpprettOppgaveTask.opprettTask(
            behandlingId = behandlingId,
            oppgavetype = OppgavetypeEYO.GOD_VED,
            objectMapper = objectMapper,
            taskService = taskService,
        )
    }

    @Transactional
    fun angreSendTilBeslutter(behandlingId: UUID) {
        behandlingService.oppdaterBehandlingStatus(
            behandlingId = behandlingId,
            status = BehandlingStatus.UTREDES,
        )
        endringshistorikkService.registrerEndring(
            behandlingId = behandlingId,
            endringType = EndringType.ANGRET_SEND_TIL_BESLUTTER,
        )
    }

    @Transactional
    fun besluttVedtak(
        behandlingId: UUID,
        beslutteVedtakDto: BeslutteVedtakDto,
    ) {
        totrinnskontrollService.validerAtBeslutterIkkeErSammeSomSaksbehandler(behandlingId)

        if (beslutteVedtakDto.godkjent) {
            godkjennVedtak(behandlingId)
        } else {
            underkjennVedtak(behandlingId = behandlingId, beslutteVedtakDto = beslutteVedtakDto)
        }
    }

    private fun godkjennVedtak(behandlingId: UUID) {
        behandlingService.oppdaterBehandlingStatus(
            behandlingId = behandlingId,
            status = BehandlingStatus.FERDIGSTILT,
        )
        endringshistorikkService.registrerEndring(
            behandlingId = behandlingId,
            endringType = EndringType.BESLUTTER_GODKJENT,
        )

        FerdigstillOppgaveTask.opprettTask(
            behandlingId = behandlingId,
            oppgavetype = OppgavetypeEYO.GOD_VED,
            objectMapper = objectMapper,
            taskService = taskService,
        )
    }

    private fun underkjennVedtak(
        behandlingId: UUID,
        beslutteVedtakDto: BeslutteVedtakDto,
    ) {
        require(!beslutteVedtakDto.begrunnelse.isNullOrBlank()) { "Begrunnelse er påkrevd ved underkjennelse" }
        require(beslutteVedtakDto.årsakerUnderkjent.isNotEmpty()) { "Minst én årsak må velges ved underkjennelse" }

        val saksbehandlerSomSendteTilBeslutter =
            totrinnskontrollService.hentSaksbehandlerSomSendteTilBeslutter(behandlingId)

        behandlingService.oppdaterBehandlingStatus(
            behandlingId = behandlingId,
            status = BehandlingStatus.UTREDES,
        )
        behandlingService.oppdaterBehandlingResultat(
            behandlingId = behandlingId,
            resultat = BehandlingResultat.IKKE_SATT,
        )
        endringshistorikkService.registrerEndring(
            behandlingId = behandlingId,
            endringType = EndringType.BESLUTTER_UNDERKJENT,
            detaljer = objectMapper.writeValueAsString(beslutteVedtakDto),
        )

        FerdigstillOppgaveTask.opprettTask(
            behandlingId = behandlingId,
            oppgavetype = OppgavetypeEYO.GOD_VED,
            objectMapper = objectMapper,
            taskService = taskService,
        )

        OpprettOppgaveTask.opprettTask(
            behandlingId = behandlingId,
            oppgavetype = OppgavetypeEYO.BEH_UND_VED,
            tilordnetSaksbehandler = saksbehandlerSomSendteTilBeslutter,
            objectMapper = objectMapper,
            taskService = taskService,
        )
    }
}
