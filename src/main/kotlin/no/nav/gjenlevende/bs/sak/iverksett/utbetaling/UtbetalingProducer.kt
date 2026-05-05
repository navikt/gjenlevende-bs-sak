package no.nav.gjenlevende.bs.sak.iverksett.utbetaling

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper

@Component
@Suppress("SpringJavaInjectionPointsAutowiringInspection") // TODO skeptisk til suppress'en her
class UtbetalingProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val properties: UtbetalingConfigProperties,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val topic = properties.utbetalingTopic

    fun sendUtbetaling(
        behandlingIdAsKey: String,
        melding: UtbetalingMelding,
    ) {
        log.info("Sender utbetaling til topic=$topic id=${melding.id} dryrun=${melding.dryrun}")
        kafkaTemplate.send(topic, behandlingIdAsKey, objectMapper.writeValueAsString(melding)).whenComplete { result, ex ->
            if (ex != null) {
                log.error("Feil ved sending av utbetaling id=${melding.id}", ex)
            } else {
                log.info("Utbetaling sendt id=${melding.id} offset=${result.recordMetadata.offset()}")
            }
        }
    }
}
