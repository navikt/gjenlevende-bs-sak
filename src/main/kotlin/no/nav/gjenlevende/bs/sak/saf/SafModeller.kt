package no.nav.gjenlevende.bs.sak.saf

import org.apache.commons.lang3.StringUtils
import org.springframework.core.io.ClassPathResource

class SafException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class SafJournalpostBrukerData(
    val dokumentoversiktBruker: Journalpostliste,
)

data class Journalpostliste(
    val journalposter: List<Journalpost>?,
)

data class SafJournalpostResponse<T>(
    val data: T? = null,
    val errors: List<SafError>? = null,
)

data class SafError(
    val message: String? = null,
    val extensions: SafExtension? = null,
)

data class SafExtension(
    val code: SafErrorCode,
    val classification: String,
)

@Suppress("EnumEntryName")
enum class SafErrorCode {
    forbidden,
    not_found,
    bad_request,
    server_error,
}

data class JournalposterForBrukerRequest(
    val brukerId: Bruker,
    val tema: List<Arkivtema>?,
    val journalposttype: List<Journalposttype>?,
    val antall: Int,
)

data class Bruker(
    val id: String,
    val type: BrukerIdType,
)

enum class BrukerIdType {
    AKTOERID,
    FNR,
    ORGNR,
}

fun JournalposterForBrukerRequest.tilSafRequestForBruker(): SafRequestForBruker =
    SafRequestForBruker(
        brukerId = brukerId,
        tema = tema,
        journalposttype = journalposttype,
        antall = antall,
    )

data class SafRequestForBruker(
    val brukerId: Bruker,
    val tema: List<Arkivtema>?,
    val journalposttype: List<Journalposttype>?,
    val journalstatus: List<String>? = emptyList(),
    val antall: Int,
)

data class SafJournalpostRequest(
    val variables: Any,
    val query: String,
)

fun String.graphqlCompatible(): String = StringUtils.normalizeSpace(this.replace("\n", ""))

fun graphqlQuery(path: String) = ClassPathResource(path).url.readText().graphqlCompatible()

data class Journalpost(
    val journalpostId: String? = null,
    val tema: String? = null,
    val behandlingstema: String? = null,
    val tittel: String? = null,
    val bruker: Bruker? = null,
    val journalforendeEnhet: String? = null,
    val kanal: String? = null,
    val eksternReferanseId: String? = null,
)

enum class Arkivtema(
    val navn: String,
) {
    AAP("Arbeidsavklaringspenger"),
    AAR("Aa-registeret"),
    AGR("Ajourhold - Grunnopplysninger"),
    ARP("Arbeidsrådgivning – psykologtester"),
    ARS("Arbeidsrådgivning – skjermet"),
    BAR("Barnetrygd"),
    BID("Bidrag"),
    BIL("Bil"),
    DAG("Dagpenger"),
    ENF("Enslig forsørger"),
    ERS("Erstatning"),
    EYB("Barnepensjon"),
    EYO("Omstillingsstønad"),
    FAR("Farskap"),
    FIP("Fiskerpensjon"),
    FOS("Forsikring"),
    FUL("Fullmakt"),
    GEN("Generell"),
    GRA("Gravferdsstønad"),
    GRU("Grunn- og hjelpestønad"),
    HEL("Helsetjenester og ortopediske hjelpemidler"),
    HJE("Hjelpemidler"),
    IAR("Inkluderende arbeidsliv"),
    IND("Tiltakspenger"),
    KLL("Klage – lønnsgaranti"),
    KON("Kontantstøtte"),
    KTA("Kontroll – anmeldelse"),
    KTR("Kontroll"),
    MED("Medlemskap"),
    MOB("Mobilitetsfremmende stønad"),
    FOR("Foreldre- og svangerskapspenger"),
    FEI("Feilutbetaling"),
    FRI("Kompensasjon for selvstendig næringsdrivende/frilansere"),
    OKO("Økonomi"),
    OMS("Omsorgspenger pleiepenger og opplæringspenger"),
    OPA("Oppfølging - Arbeidsgiver"),
    OPP("Oppfølging"),
    PEN("Pensjon"),
    PER("Permittering og masseoppsigelser"),
    REH("Rehabilitering"),
    REK("Rekruttering og stilling"),
    RPO("Retting av personopplysninger"),
    RVE("Rettferdsvederlag"),
    SAA("Sanksjon - Arbeidsgiver"),
    SAK("Saksomkostninger"),
    SAP("Sanksjon - Person"),
    SER("Serviceklager"),
    SIK("Sikkerhetstiltak"),
    STO("Regnskap/utbetaling"),
    SUP("Supplerende stønad"),
    SYK("Sykepenger"),
    SYM("Sykmeldinger"),
    TIL("Tiltak"),
    TRK("Trekkhåndtering"),
    TRY("Trygdeavgift"),
    TSO("Tilleggsstønad"),
    TSR("Tilleggsstønad arbeidssøkere"),
    UFM("Unntak fra medlemskap"),
    UFO("Uføretrygd"),
    UKJ("Ukjent"),
    VEN("Ventelønn"),
    YRA("Yrkesrettet attføring"),
    YRK("Yrkesskade"),
}

enum class Journalposttype {
    I,
    U,
    N,
}
