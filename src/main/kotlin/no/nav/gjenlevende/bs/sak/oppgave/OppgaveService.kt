package no.nav.gjenlevende.bs.sak.oppgave

import no.nav.gjenlevende.bs.sak.behandling.Behandling
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OppgaveService {
// private val oppgaveClient: OppgaveClient
    private val logger = LoggerFactory.getLogger(OppgaveService::class.java)

    fun opprettBehandleSakOppgave(
        behandling: Behandling,
        saksbehandler: String,
    ) {
        logger.info("Skal opprette behandle sak oppgave for behandling=${behandling.id} saksbehandler=$saksbehandler")

        val oppgaveRequest =
            OppgaveRequest(
                tittel = "Behandle sak for behandling=${behandling.id}",
                beskrivelse = "Bahandle sak oppgave for behandling=${behandling.id}",
                saksbehandler = saksbehandler,
                behandlingsId = behandling.id,
            )
        // oppgaveClient.opprettOppgave(oppgaveRequest = oppgaveRequest)
    }
}

data class OppgaveRequest(
    val tittel: String,
    val beskrivelse: String,
    val saksbehandler: String,
    val behandlingsId: UUID,
)
