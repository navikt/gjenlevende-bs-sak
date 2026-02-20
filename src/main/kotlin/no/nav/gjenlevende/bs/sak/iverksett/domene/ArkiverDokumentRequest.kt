package no.nav.gjenlevende.bs.sak.iverksett.domene

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import no.nav.gjenlevende.bs.sak.saf.Arkivtema

data class ArkiverDokumentRequest(
    @field:NotBlank val fnr: String,
    val forsøkFerdigstill: Boolean,
    @field:NotEmpty val hoveddokumentvarianter: List<Dokument>,
    val vedleggsdokumenter: List<Dokument> = emptyList(),
    val fagsakId: String? = null,
    val tema: Arkivtema,
    val journalførendeEnhet: String? = null,
    val førsteside: Førsteside? = null,
    val eksternReferanseId: String? = null,
    val avsenderMottaker: AvsenderMottaker? = null,
)

data class Førsteside(
    val språkkode: Språkkode = Språkkode.NB,
    val navSkjemaId: String,
    val overskriftstittel: String,
)

enum class Språkkode {
    NB,
    NN,
}

class Dokument(
    @field:NotEmpty val dokument: ByteArray,
    @field:NotEmpty val filtype: Filtype,
    val filnavn: String? = null,
    val tittel: String? = null,
    @field:NotEmpty val dokumenttype: Dokumenttype,
)

enum class Filtype {
    PDFA,
    JSON,
}

data class AvsenderMottaker(
    val id: String?,
    val idType: AvsenderMottakerIdType?,
    val navn: String,
)

enum class AvsenderMottakerIdType {
    FNR,
    HPRNR,
    NULL,
    ORGNR,
    UKJENT,
    UTL_ORG,
}
