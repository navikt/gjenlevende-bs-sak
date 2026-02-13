package no.nav.gjenlevende.bs.sak.felles.sporbar

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class Sporing(
    @CreatedBy
    val opprettetAv: String = "",
    @CreatedDate
    val opprettetTid: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
    @LastModifiedBy
    val endretAv: String = "",
    @LastModifiedDate
    val endretTid: LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
)
