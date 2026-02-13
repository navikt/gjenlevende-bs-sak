package no.nav.gjenlevende.bs.sak.felles.sporbar

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

data class Sporing(
    @CreatedBy
    val opprettetAv: String = "",
    @CreatedDate
    val opprettetTid: LocalDateTime = LocalDateTime.now(),
    @LastModifiedBy
    val endretAv: String = "",
    @LastModifiedDate
    val endretTid: LocalDateTime = LocalDateTime.now(),
)
