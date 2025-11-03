package no.nav.gjenlevende.bs.sak.model

import java.time.LocalDateTime

data class InfotrygdData(
    val id: String,
    val message: String,
    val timestamp: LocalDateTime,
    val status: String,
)

data class InfotrygdHealthResponse(
    val service: String,
    val status: String,
    val timestamp: LocalDateTime,
)
