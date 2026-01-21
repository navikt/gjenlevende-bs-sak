package no.nav.gjenlevende.bs.sak.brev

import no.nav.gjenlevende.bs.sak.brev.domain.BrevRequest
import no.nav.gjenlevende.bs.sak.felles.sporbar.Sporbar
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("brev")
data class Brev(
    @Id
    val behandlingId: UUID,
    val brevJson: BrevRequest,
    val brevPdf: ByteArray? = null,
    val saksbehandler: String? = null,
    val beslutter: String? = null,
    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)
    val sporbar: Sporbar = Sporbar(),
)
