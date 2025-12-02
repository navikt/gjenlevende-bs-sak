package no.nav.gjenlevende.bs.sak.fagsak.domain

import no.nav.gjenlevende.bs.sak.felles.sporbar.Sporbar
import no.nav.gjenlevende.bs.sak.infotrygd.dto.StønadType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

data class Fagsak(
    val id: UUID,
    val fagsakPersonId: UUID,
    val personIdenter: Set<PersonIdent>,
    val eksternId: Long,
    val stønadstype: StønadType,
    val sporbar: Sporbar,
) {
    fun hentAktivIdent(): String = personIdenter.maxByOrNull { it.sporbar.endret.endretTid }?.ident ?: error("Fant ingen ident på fagsak $id")
}

@Table("fagsak")
data class FagsakDomain(
    @Id
    val id: UUID = UUID.randomUUID(),
    val fagsakPersonId: UUID,
    val eksternId: Long = 0,
    @Column("stonadstype")
    val stønadstype: StønadType,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)
    val sporbar: Sporbar = Sporbar(),
)

fun FagsakDomain.tilFagsakMedPerson(personIdenter: Set<PersonIdent>): Fagsak =
    Fagsak(
        id = id,
        fagsakPersonId = fagsakPersonId,
        personIdenter = personIdenter,
        eksternId = eksternId,
        stønadstype = stønadstype,
        sporbar = sporbar,
    )
