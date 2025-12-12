package no.nav.gjenlevende.bs.sak.saf

import no.nav.gjenlevende.bs.sak.config.SafConfig
import no.nav.gjenlevende.bs.sak.fagsak.FagsakPersonService
import no.nav.gjenlevende.bs.sak.pdl.PdlService
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class SafService(
    private val safClient: SafClient,
    private val fagsakPersonService: FagsakPersonService,
) {
    private val logger = LoggerFactory.getLogger(PdlService::class.java)

    fun hentJournalposterForIdent(ident: String): List<Journalpost>? {
        val data =
            safClient.utførQuery(
                query = SafConfig.hentJournalposterBrukerQuery,
                variables = JournalposterForBrukerRequest(Bruker(ident, BrukerIdType.FNR), emptyList(), emptyList(), 200),
                responstype = object : ParameterizedTypeReference<SafJournalpostResponse<SafJournalpostBrukerData>>() {},
                operasjon = "hentJournalposterForBrukerId",
            ) ?: throw SafException("Fant ingen person i SAF for brukerId")

        return data.dokumentoversiktBruker.journalposter
    }

    fun finnDokumenterForPerson(fagsakPersonId: UUID): List<DokumentinfoDto> {
        val ident = fagsakPersonService.hentAktivIdent(fagsakPersonId)
        val journalposter = hentJournalposterForIdent(ident)

        return journalposter
            ?.flatMap { journalpost -> journalpost.dokumenter?.map { tilDokumentInfoDto(it, journalpost) } ?: emptyList() } ?: emptyList()
    }

    private fun tilDokumentInfoDto(
        dokumentInfo: DokumentInfo,
        journalpost: Journalpost,
    ): DokumentinfoDto =
        DokumentinfoDto(
            dokumentinfoId = dokumentInfo.dokumentInfoId,
            filnavn = dokumentInfo.dokumentvarianter?.find { it.variantformat == Dokumentvariantformat.ARKIV }?.filnavn,
            tittel = dokumentInfo.tittel ?: "Tittel mangler",
            journalpostId = journalpost.journalpostId,
            dato = mestRelevanteDato(journalpost),
            journalstatus = journalpost.journalstatus,
            journalposttype = journalpost.journalposttype,
            logiskeVedlegg = dokumentInfo.logiskeVedlegg?.map { LogiskVedleggDto(tittel = it.tittel) } ?: emptyList(),
            avsenderMottaker = journalpost.avsenderMottaker,
            tema = journalpost.tema,
            harSaksbehandlerTilgang = dokumentInfo.dokumentvarianter?.find { it.variantformat == Dokumentvariantformat.ARKIV }?.saksbehandlerHarTilgang ?: false,
        )

    fun mestRelevanteDato(journalpost: Journalpost): LocalDateTime? = journalpost.datoMottatt ?: journalpost.relevanteDatoer?.maxByOrNull { datoTyperSortert(it.datotype) }?.dato

    private fun datoTyperSortert(datoType: String) =
        when (datoType) {
            "DATO_JOURNALFOERT" -> 4
            "DATO_REGISTRERT" -> 3
            "DATO_DOKUMENT" -> 2
            "DATO_OPPRETTET" -> 1
            else -> 0
        }
}
