package no.nav.gjenlevende.bs.sak.iverksett.utbetaling

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.util.UUID

@Profile("!prod")
@Component
class DevSimuleringAutoCompleter(
    private val objectMapper: ObjectMapper,
    private val simuleringResponseListener: SimuleringResponseListener,
) {
    fun autoFullfor(simuleringId: UUID) {
        val mockRespons =
            SimuleringResponse(
                perioder =
                    listOf(
                        SimuleringPeriode(
                            fom = LocalDate.now().withDayOfMonth(1),
                            tom = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1),
                            utbetalinger =
                                listOf(
                                    SimuleringUtbetaling(
                                        fagsystem = "GJENLEVENDE_BS",
                                        sakId = "mock-sakId",
                                        utbetalesTil = 12345699999L,
                                        stønadstype = "GJENLEVENDE_BARNETILSYN",
                                        tidligereUtbetalt = 0,
                                        nyttBeløp = 5000,
                                    ),
                                ),
                        ),
                    ),
            )

        val record =
            ConsumerRecord<String, String>(
                "mock-topic",
                0,
                0L,
                simuleringId.toString(),
                objectMapper.writeValueAsString(mockRespons),
            )

        simuleringResponseListener.mottaSimuleringsresultat(record)
    }
}
