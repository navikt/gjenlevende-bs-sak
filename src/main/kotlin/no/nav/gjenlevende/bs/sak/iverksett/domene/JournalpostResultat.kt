package no.nav.gjenlevende.bs.sak.iverksett.domene

import java.time.LocalDateTime

/**
 * Map av [JournalpostResultat] per mottakerIdent
 */
data class JournalpostResultatMap(
    val map: Map<String, JournalpostResultat> = emptyMap(),
) {
    operator fun plus(tillegg: Map<String, JournalpostResultat>): JournalpostResultatMap = JournalpostResultatMap(this.map + tillegg)

    fun isNotEmpty() = map.isNotEmpty()
}

data class JournalpostResultat(
    val journalpostId: String,
    val journalført: LocalDateTime = LocalDateTime.now(),
)
