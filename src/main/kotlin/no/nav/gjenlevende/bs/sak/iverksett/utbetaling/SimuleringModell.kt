package no.nav.gjenlevende.bs.sak.iverksett.utbetaling

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class SimuleringResponse(
    val perioder: List<SimuleringPeriode>,
)

data class SimuleringPeriode(
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val fom: LocalDate,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val tom: LocalDate,
    val utbetalinger: List<SimuleringUtbetaling>,
)

data class SimuleringUtbetaling(
    val fagsystem: String,
    val sakId: String,
    val utbetalesTil: Long,
    val stønadstype: String,
    val tidligereUtbetalt: Int,
    val nyttBeløp: Int,
)

@Table("simulering")
data class Simulering(
    @Id
    val behandlingId: UUID,
    val status: SimuleringStatus,
    val respons: SimuleringResponse? = null,
    val opprettetTid: LocalDateTime = LocalDateTime.now(),
)

enum class SimuleringStatus {
    VENTER,
    FERDIG,
    FEILET,
}
