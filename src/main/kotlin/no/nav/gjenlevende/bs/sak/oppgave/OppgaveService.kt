package no.nav.gjenlevende.bs.sak.oppgave

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.Pattern
import no.nav.gjenlevende.bs.sak.behandling.Behandling
import no.nav.gjenlevende.bs.sak.fagsak.FagsakPersonRepository
import no.nav.gjenlevende.bs.sak.fagsak.FagsakRepository
import no.nav.gjenlevende.bs.sak.fagsak.domain.Fagsak
import no.nav.gjenlevende.bs.sak.fagsak.domain.FagsakPerson
import no.nav.gjenlevende.bs.sak.fagsak.domain.Personident
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class OppgaveService(
    private val fagsakRepository: FagsakRepository,
    private val fagsakPersonRepository: FagsakPersonRepository,
    private val oppgaveClient: OppgaveClient,
    private val oppgaveRepository: OppgaveRepository,
) {
    private val logger = LoggerFactory.getLogger(OppgaveService::class.java)

    fun opprettBehandleSakOppgave(
        behandling: Behandling,
        saksbehandler: String,
        tildeltEnhetsnr: String,
    ) {
        logger.info("Skal opprette behandle sak oppgave for behandling=${behandling.id} saksbehandler=$saksbehandler")

        val fagsak = fagsakRepository.findByIdOrNull(behandling.fagsakId) ?: throw IllegalStateException("Finner ikke fagsak med id=${behandling.fagsakId}")
        val fagsakPerson = fagsakPersonRepository.findByIdOrNull(fagsak.fagsakPersonId) ?: throw IllegalStateException("Finner ikke fagsakPerson med id=${fagsak.fagsakPersonId}")
        val personident: Personident = fagsakPerson.aktivIdent()

        val oppgaveRequest = lagOpprettBehandleSakOppgaveRequest(personident, fagsak, behandling, saksbehandler, tildeltEnhetsnr)

        val oppgave = oppgaveClient.opprettOppgaveM2M(oppgaveRequest = oppgaveRequest)

        oppgaveRepository.insert(
            Oppgave(
                behandlingId = behandling.id,
                gsakOppgaveId = oppgave.id ?: throw IllegalStateException("Oppgave-respons mangler id"),
                type = OppgavetypeEYO.BEH_SAK.name,
            ),
        )

        logger.info("Oppgave med gsakOppgaveId=${oppgave.id} lagret for behandling=${behandling.id}")
    }
}

private fun lagOpprettBehandleSakOppgaveRequest(
    personident: Personident,
    fagsak: Fagsak,
    behandling: Behandling,
    saksbehandler: String,
    tildeltEnhetsnr: String,
): LagOppgaveRequest =
    LagOppgaveRequest(
        personident = personident.ident,
        saksreferanse = fagsak.eksternId.toString(), // TODO sjekk om dette burde være "behandling-eksternid"?
        prioritet = OppgavePrioritet.NORM,
        tema = Tema.EYO,
        behandlingstema = fagsak.stønadstype.behandlingstema,
        fristFerdigstillelse = OppgaveUtil.lagFristForOppgave().format(DateTimeFormatter.ISO_DATE),
        aktivDato = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
        oppgavetype = OppgavetypeEYO.BEH_SAK,
        beskrivelse = "Behandle sak oppgave for barnetilsynbehandling=${behandling.id}-${fagsak.stønadstype.name}",
        tilordnetRessurs = saksbehandler,
        behandlesAvApplikasjon = "gjenlevende-bs-sak", // Kan kun feilregistreres av saksbehandler i gosys? Må ferdigstilles av applikasjon?
        tildeltEnhetsnr = tildeltEnhetsnr,
    )

fun FagsakPerson.aktivIdent(): Personident =
    this.identer.maxByOrNull { it.sporbar.endret.endretTid }
        ?: throw IllegalStateException("FagsakPerson har ingen identer")

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OppgaveDto(
    val id: Long? = null,
    val identer: List<OppgavePersonident>? = null,
    val tildeltEnhetsnr: String? = null,
    val endretAvEnhetsnr: String? = null,
    val opprettetAvEnhetsnr: String? = null,
    val journalpostId: String? = null,
    val journalpostkilde: String? = null,
    val behandlesAvApplikasjon: String? = null,
    val saksreferanse: String? = null,
    val samhandlernr: String? = null,
    @field:Pattern(regexp = "[0-9]{13}")
    val aktoerId: String? = null,
    @field:Pattern(regexp = "[0-9]{11,13}")
    val personident: String? = null,
    val orgnr: String? = null,
    val tilordnetRessurs: String? = null,
    val beskrivelse: String? = null,
    val temagruppe: String? = null,
    val tema: Tema,
    val behandlingstema: String? = null,
    val oppgavetype: String,
    val behandlingstype: String? = null,
    val versjon: Int? = null,
    val mappeId: Long? = null,
    val fristFerdigstillelse: String? = null,
    val aktivDato: String? = null,
    val opprettetTidspunkt: String? = null,
    val opprettetAv: String? = null,
    val endretAv: String? = null,
    val ferdigstiltTidspunkt: String? = null,
    val endretTidspunkt: String? = null,
    val prioritet: OppgavePrioritet? = null,
    val status: StatusEnum? = null,
    private var metadata: MutableMap<String, String>? = null,
)

enum class StatusEnum {
    OPPRETTET,
    AAPNET,
    UNDER_BEHANDLING,
    FERDIGSTILT,
    FEILREGISTRERT,
}

enum class OppgavePrioritet {
    HOY,
    NORM,
    LAV,
}

// mulige oppgavetyper for tema EYO (Februar 2026)
enum class OppgavetypeEYO(
    val term: String,
) {
    BEH_SAK(
        term = "Behandle sak",
    ),
    GOD_VED(
        term = "Godkjenne vedtak",
    ),
    BEH_UND_VED(
        term = "Behandle underkjent vedtak",
    ),
    INNH_DOK(
        term = "Innhent dokumentasjon",
    ),
    VURD_NOTAT(
        term = "Vurder notat",
    ),
    VURD_BREV(
        term = "Vurder brev",
    ),
    GEN(
        term = "Generell",
    ),
    VURD_HENV(
        term = "Vurder henvendelse",
    ),
    VUR_KONS_YTE(
        term = "Vurder konsekvens for ytelse",
    ),
    KONT_BRUK(
        term = "Kontakt bruker",
    ),
    KRA_DOD(
        term = "Død",
    ),
    BEH_SED(
        term = "Behandle SED",
    ),
    TVU_FOR(
        term = "Tvungen forvaltning",
    ),
    RETUR(
        term = "Behandle returpost",
    ),
    JFR(
        term = "Journalføring",
    ),
    KON_UTG_SCA_DOK(
        term = "Kontroller utgående skannet dokument",
    ),
    FDR(
        term = "Fordeling",
    ),
    JFR_UT(
        term = "Journalføring utgående",
    ),
    VUR_SVAR(
        term = "Vurder svar",
    ),
    SVAR_IK_MOT(
        term = "Svar ikke mottatt",
    ),
}

data class OppgavePersonident(
    val ident: String?,
    val gruppe: IdentGruppe?,
)

enum class IdentGruppe {
    AKTOERID,
    FOLKEREGISTERIDENT,
    NPID,
    ORGNR,
    SAMHANDLERNR,
}

enum class Tema {
    EYO,
}
