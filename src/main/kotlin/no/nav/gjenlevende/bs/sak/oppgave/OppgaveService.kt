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
import no.nav.gjenlevende.bs.sak.util.findByIdOrThrow
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class OppgaveService(
    private val fagsakRepository: FagsakRepository,
    private val fagsakPersonRepository: FagsakPersonRepository,
    private val oppgaveClient: OppgaveClient,
) {
    private val logger = LoggerFactory.getLogger(OppgaveService::class.java)

    fun opprettBehandleSakOppgave(
        behandling: Behandling,
        saksbehandler: String,
        tildeltEnhetsnr: String,
    ) {
        logger.info("Skal opprette behandle sak oppgave for behandling=${behandling.id} saksbehandler=$saksbehandler")

        val fagsak = fagsakRepository.findByIdOrThrow(behandling.fagsakId)
        val fagsakPerson = fagsakPersonRepository.findByIdOrThrow(fagsak.fagsakPersonId)
        val gjeldendePersonIdent: Personident = fagsakPerson.aktivIdent()

        val oppgave = lagOpprettBehandleSakOppgaveRequest(gjeldendePersonIdent, fagsak, behandling, saksbehandler, tildeltEnhetsnr)
        oppgaveClient.opprettOppgaveM2M(oppgaveRequest = oppgave)
    }
}

private fun lagOpprettBehandleSakOppgaveRequest(
    gjeldendePersonIdent: Personident,
    fagsak: Fagsak,
    behandling: Behandling,
    saksbehandler: String,
    tildeltEnhetsnr: String,
): LagOppgaveRequest =
    LagOppgaveRequest(
        personident = gjeldendePersonIdent.ident,
        saksreferanse = fagsak.eksternId.toString(), // TODO sjekk om dette burde være "behandling-eksternid"?
        prioritet = OppgavePrioritet.NORM,
        tema = Tema.EYO, // TODO finn tema for BARNETILSYN GJENLEVENDE EYO = omstilling
        // behandlingstema = "ae0290", // Samhandling EM-PE-OM ... TODO finn behandlingstema for BARNETILSYN GJENLEVENDE, ab0224 ab0028(Hører til ENF - barnetilsyn)? ab0224
        behandlingstype = "ae0290", // TODO vil legge inn behandlings_tema_ Gjenlevende bs når det er på plass - her bruker vi kun type.
        fristFerdigstillelse = lagFristForOppgave().format(DateTimeFormatter.ISO_DATE),
        aktivDato = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
        oppgavetype = OppgavetypeEYO.GEN, // TODO legg inn BehandleSak som option,
        beskrivelse = "Behandle sak oppgave for barnetilsynbehandling=${behandling.id}-${fagsak.stønadstype.name}",
        tilordnetRessurs = saksbehandler,
        behandlesAvApplikasjon = "gjenlevende-bs-sak", // Kan kun feilregistreres av saksbehandler i gosys? Må ferdigstilles av applikasjon?
        tildeltEnhetsnr = tildeltEnhetsnr,
    )

fun lagFristForOppgave(gjeldendeTid: LocalDateTime = now()): LocalDate {
    val frist =
        when (gjeldendeTid.dayOfWeek) {
            DayOfWeek.FRIDAY -> fristBasertPåKlokkeslett(gjeldendeTid.plusDays(2))
            DayOfWeek.SATURDAY -> fristBasertPåKlokkeslett(gjeldendeTid.plusDays(2).withHour(8))
            DayOfWeek.SUNDAY -> fristBasertPåKlokkeslett(gjeldendeTid.plusDays(1).withHour(8))
            else -> fristBasertPåKlokkeslett(gjeldendeTid)
        }

    return when (frist.dayOfWeek) {
        DayOfWeek.SATURDAY -> frist.plusDays(2)
        DayOfWeek.SUNDAY -> frist.plusDays(1)
        else -> frist
    }
}

fun now(): LocalDateTime = LocalDateTime.now()

private fun fristBasertPåKlokkeslett(gjeldendeTid: LocalDateTime): LocalDate =
    if (gjeldendeTid.hour >= 12) {
        gjeldendeTid.plusDays(2).toLocalDate()
    } else {
        gjeldendeTid.plusDays(1).toLocalDate()
    }

fun FagsakPerson.aktivIdent(): Personident =
    this.identer.maxByOrNull { it.sporbar.endret.endretTid }
        ?: throw IllegalStateException("FagsakPerson har ingen identer")

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Oppgave(
    val id: Long? = null,
    val identer: List<OppgaveIdentV2>? = null,
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

// mulige oppgavetyper for tema EYO - vil legge in behandle sak og beslutter oppgave her:
// https://github.com/navikt/oppgave/blob/70715a7b37e619be0e30079f5fdcf957effe2ba2/src/main/resources/data/oppgavetyper.json#L577
enum class OppgavetypeEYO {
    INNH_DOK,
    VURD_NOTAT,
    VURD_BREV,
    GEN,
    VURD_HENV,
    VUR_KONS_YTE,
    KONT_BRUK,
    KRA_DOD,
    BEH_SED,
    TVU_FOR,
    RETUR,
    JFR,
    KON_UTG_SCA_DOK,
    FDR,
    JFR_UT,
    VUR_SVAR,
    SVAR_IK_MOT,
}

data class OppgaveIdentV2(
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
