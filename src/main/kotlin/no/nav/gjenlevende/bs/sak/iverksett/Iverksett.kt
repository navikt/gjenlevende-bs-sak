package no.nav.gjenlevende.bs.sak.iverksett

import no.nav.gjenlevende.bs.sak.felles.sporbar.Sporbar
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("iverksett")
data class Iverksett(
    @Id
    val behandlingId: UUID,
    val eksternReferanseId: UUID? = null,
    val beslutterEnhet: String,
    val journalpostResultatId: String? = null,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)
    val sporbar: Sporbar = Sporbar(),
)
