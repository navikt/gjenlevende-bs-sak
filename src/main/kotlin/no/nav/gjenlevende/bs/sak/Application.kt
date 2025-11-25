package no.nav.gjenlevende.bs.sak

import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class Application

private val logger = LoggerFactory.getLogger(Application::class.java)

fun main(args: Array<String>) {
    runApplication<Application>(*args)
    logger.info(MarkerFactory.getMarker("TEAM_LOGS"), "Gjenlevende-BS-Sak bruker Team Logs, Bink Bonk!")
}
