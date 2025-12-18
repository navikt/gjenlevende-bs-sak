package no.nav.gjenlevende.bs.sak.brev

import no.nav.gjenlevende.bs.sak.felles.sporbar.Sporbar
import org.postgresql.util.PGobject
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("brev")
data class Brev(
    @Id
    val behandlingsId: UUID,
    val brevJson: PGobject,
    val brevPdf: ByteArray? = null,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)
    val sporbar: Sporbar = Sporbar(),
)
