package no.nav.gjenlevende.bs.sak.iverksett.domene

data class ArkiverDokumentResponse(
    val dokumenter: List<DokumentInfoResponse>,
    val journalpostId: String,
    val journalpostferdigstilt: Boolean,
)

data class DokumentInfoResponse(
    val dokumentInfoId: String,
)
