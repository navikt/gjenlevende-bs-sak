package no.nav.gjenlevende.bs.sak.iverksett.metadata

import no.nav.gjenlevende.bs.sak.iverksett.domene.Dokumentkategori
import no.nav.gjenlevende.bs.sak.iverksett.domene.Dokumenttype
import no.nav.gjenlevende.bs.sak.iverksett.domene.Fagsystem
import no.nav.gjenlevende.bs.sak.iverksett.domene.JournalpostType
import no.nav.gjenlevende.bs.sak.saf.Arkivtema
import org.springframework.stereotype.Component

@Component
object SkolepengerVedtaksbrevMetadata : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.EY
    override val tema: String = "EYO"
    override val behandlingstema: Behandlingstema = Behandlingstema.Skolepenger
    override val kanal: String? = null
    override val dokumenttype: Dokumenttype = Dokumenttype.VEDTAKSBREV_SKOLEPENGER
    override val tittel: String? = null
    override val brevkode: String = "EYO_BREV_SKOLEPENGER_VEDTAK"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.VB
}
