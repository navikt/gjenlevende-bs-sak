package no.nav.gjenlevende.bs.sak.iverksett.brev

data class OpprettJournalpostRequest(
    val tittel: String,
    val journalposttype: Journalposttype,
    val tema: String,
    val kanal: String? = null,
    val behandlingstema: String? = null,
    val journalfoerendeEnhet: String? = null,
    val avsenderMottaker: AvsenderMottaker? = null,
    val bruker: Bruker? = null,
    val sak: Sak? = null,
    val dokumenter: List<Dokument>,
    val eksternReferanseId: String? = null,
)

data class AvsenderMottaker(
    val id: String? = null,
    val idType: IdType? = null,
    val navn: String? = null,
)

enum class IdType {
    FNR,
    ORGNR,
    HPRNR,
    UTL_ORG,
}

data class Bruker(
    val id: String,
    val idType: BrukerIdType,
)

enum class BrukerIdType {
    FNR,
    ORGNR,
    AKTOERID,
}

data class Sak(
    val sakstype: Sakstype,
    val fagsakId: String? = null,
    val fagsaksystem: String? = null,
)

enum class Sakstype {
    FAGSAK,
    GENERELL_SAK,
}

data class Dokument(
    val tittel: String,
    val brevkode: String? = null,
    val dokumentvarianter: List<DokumentVariant>,
)

data class DokumentVariant(
    val filtype: String,
    val variantformat: String,
    val fysiskDokument: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DokumentVariant
        return filtype == other.filtype &&
            variantformat == other.variantformat &&
            fysiskDokument.contentEquals(other.fysiskDokument)
    }

    override fun hashCode(): Int {
        var result = filtype.hashCode()
        result = 31 * result + variantformat.hashCode()
        result = 31 * result + fysiskDokument.contentHashCode()
        return result
    }
}

enum class Journalposttype {
    INNGAAENDE,
    UTGAAENDE,
    NOTAT,
}

data class OpprettJournalpostResponse(
    val journalpostId: String,
    val journalpostferdigstilt: Boolean,
    val dokumenter: List<DokumentInfo>? = null,
)

data class DokumentInfo(
    val dokumentInfoId: String,
)

data class FerdigstillJournalpostRequest(
    val journalfoerendeEnhet: String,
)
