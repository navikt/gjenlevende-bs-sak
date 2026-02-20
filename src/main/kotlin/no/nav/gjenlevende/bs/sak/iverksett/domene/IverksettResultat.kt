package no.nav.gjenlevende.bs.sak.iverksett.domene

import no.nav.gjenlevende.bs.sak.felles.sporbar.Sporbar
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("iverksett_resultat")
data class IverksettResultat(
    @Id
    val behandlingId: UUID,
    val journalpostResultat: JournalpostResultatMap = JournalpostResultatMap(),
    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)
    val sporbar: Sporbar = Sporbar(),
)
