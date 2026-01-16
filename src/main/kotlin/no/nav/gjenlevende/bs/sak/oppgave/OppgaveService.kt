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
class OppgaveService(private val fagsakRepository: FagsakRepository,
                     private val fagsakPersonRepository: FagsakPersonRepository,
                     private val oppgaveClient: OppgaveClient) {
    private val logger = LoggerFactory.getLogger(OppgaveService::class.java)

    fun opprettBehandleSakOppgave(
        behandling: Behandling,
        saksbehandler: String,
    ) {
        logger.info("Skal opprette behandle sak oppgave for behandling=${behandling.id} saksbehandler=$saksbehandler")

        val fagsak = fagsakRepository.findByIdOrThrow(behandling.fagsakId)
        val fagsakPerson = fagsakPersonRepository.findByIdOrThrow(fagsak.fagsakPersonId)
        val gjeldendePersonIdent: Personident = fagsakPerson.aktivIdent()

        val oppgave = lagOpprettBehandleSakOppgaveRequest(gjeldendePersonIdent, fagsak, behandling, saksbehandler)
        oppgaveClient.opprettOppgave(oppgave = oppgave)
    }
}

private fun lagOpprettBehandleSakOppgaveRequest(
    gjeldendePersonIdent: Personident,
    fagsak: Fagsak,
    behandling: Behandling,
    saksbehandler: String,
): Oppgave {
    val oppgave =
        Oppgave(
            aktoerId = null,
            personident = gjeldendePersonIdent.ident,
            orgnr = null,
            samhandlernr = null,
            saksreferanse = fagsak.eksternId.toString(), // TODO sjekk om dette burde være behandling-eksternid?
            journalpostId = null,
            prioritet = OppgavePrioritet.NORM,
            tema = Tema.ENF, // TODO finn tema for BARNETILSYN GJENLEVENDE EYO = omstilling
            tildeltEnhetsnr = "4489", // TODO finn enhetsnummer for BARNETILSYN GJENLEVENDE 4817 4806 ??? 4817
            behandlingstema = "ab0028", // TODO finn behandlingstema for BARNETILSYN GJENLEVENDE, ab0224 ab0028(Hører til ENF - barnetilsyn)? ab0224
            behandlingstype = null,
            fristFerdigstillelse = lagFristForOppgave().format(DateTimeFormatter.ISO_DATE),
            aktivDato = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
            oppgavetype = Oppgavetype.BehandleSak.value,
            beskrivelse = "Behandle sak oppgave for behandling=${behandling.id}-${fagsak.stønadstype.name}",
            tilordnetRessurs = saksbehandler,
            behandlesAvApplikasjon = null, // TODO sett behandlesAvApplikasjon for BARNETILSYN GJENLEVENDE
            mappeId = null,
        )
    return oppgave
}

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

private fun fristBasertPåKlokkeslett(gjeldendeTid: LocalDateTime): LocalDate {
    return if (gjeldendeTid.hour >= 12) {
        gjeldendeTid.plusDays(2).toLocalDate()
    } else {
        gjeldendeTid.plusDays(1).toLocalDate()
    }
}

fun FagsakPerson.aktivIdent(): Personident {
    return this.identer.maxByOrNull { it.sporbar.endret.endretTid }
        ?: throw IllegalStateException("FagsakPerson har ingen identer")
}

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
    val tema: Tema? = null,
    val behandlingstema: String? = null,
    val oppgavetype: String? = null,
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


enum class Oppgavetype(
    val value: String,
) {
    BehandleSak("BEH_SAK"),
    Journalføring("JFR"),
    GodkjenneVedtak("GOD_VED"),
    BehandleUnderkjentVedtak("BEH_UND_VED"),
    Fordeling("FDR"),
    BehandleReturpost("RETUR"),
    BehandleSED("BEH_SED"),
    FordelingSED("FDR_SED"),
    Fremlegg("FREM"),
    Generell("GEN"),
    InnhentDokumentasjon("INNH_DOK"),
    JournalføringUtgående("JFR_UT"),
    KontaktBruker("KONT_BRUK"),
    KontrollerUtgåendeSkannetDokument("KON_UTG_SCA_DOK"),
    SvarIkkeMottatt("SVAR_IK_MOT"),
    VurderDokument("VUR"),
    VurderHenvendelse("VURD_HENV"),
    VurderInntekt("VURD_INNT"),
    VurderKonsekvensForYtelse("VUR_KONS_YTE"),
    VurderLivshendelse("VURD_LIVS"),
    VurderSvar("VUR_SVAR"),
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

enum class Tema{
    ENF, // TODO slett eksempel;
}
