package no.nav.gjenlevende.bs.sak.iverksett.metadata

import no.nav.gjenlevende.bs.sak.iverksett.domene.Dokumentkategori
import no.nav.gjenlevende.bs.sak.iverksett.domene.Dokumenttype
import no.nav.gjenlevende.bs.sak.iverksett.domene.Fagsystem
import no.nav.gjenlevende.bs.sak.iverksett.domene.JournalpostType
import no.nav.gjenlevende.bs.sak.saf.Arkivtema
import org.springframework.stereotype.Component

@Component
object BarnetilsynVedtaksbrevMetadata : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.EY
    override val tema: Arkivtema = Arkivtema.EYO
    override val behandlingstema: Behandlingstema = Behandlingstema.Barnetilsyn
    override val kanal: String? = null // TODO https://confluence.adeo.no/spaces/BOA/pages/316407153/Utsendingskanal
    override val dokumenttype: Dokumenttype = Dokumenttype.VEDTAKSBREV_BARNETILSYN
    override val tittel: String? = null
    override val brevkode: String = "EYO_BREV_BARNETILSYN_VEDTAK"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.VB
}
