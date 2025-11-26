package no.nav.gjenlevende.bs.sak.test

import no.nav.familie.leader.LeaderClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * En enkel spike for å teste bruk av LeaderClient i en scheduler.
 * Denne klassen kan slettes etter at testing er utført
 */
@Component
class SchedulerLeaderSpike {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 30000)
    fun runEvery30Seconds() {
        println("SchedulerLeaderSpike kjører hvert 30 sekund...")
        when (LeaderClient.isLeader()) {
            true -> logger.info("Leder: Utfører kun leder-oppgave.")
            false -> logger.info("Ikke leder: gjør ikke noe.")
            null -> logger.warn("LeaderClient.isLeader() returnerte null")
        }
    }
}
