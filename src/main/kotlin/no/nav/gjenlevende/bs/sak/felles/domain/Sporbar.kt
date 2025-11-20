package no.nav.gjenlevende.bs.sak.felles.domain

import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.relational.core.mapping.Embedded
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class Sporbar(
    val opprettetAv: String,
    val opprettetAvType: AktørType,
    val opprettetTid: LocalDateTime,
    @LastModifiedBy
    @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY)
    val endret: Endret,
)

data class Endret(
    val endretAv: String,
    val endretAvType: AktørType,
    val endretTid: LocalDateTime,
)

object SporbarUtils {
    fun now(): LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
}
