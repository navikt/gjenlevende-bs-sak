package no.nav.gjenlevende.bs.sak.fagsak.domain

import no.nav.gjenlevende.bs.sak.felles.sporbar.Sporing
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.MappedCollection
import java.util.UUID

data class FagsakPerson(
    @Id
    val id: UUID = UUID.randomUUID(),
    @MappedCollection(idColumn = "fagsak_person_id")
    val identer: Set<Personident>,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)
    val sporing: Sporing = Sporing(),
)

data class Personident(
    @Id
    val ident: String,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)
    val sporing: Sporing = Sporing(),
)
