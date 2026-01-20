package no.nav.gjenlevende.bs.sak.config

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment

@Configuration
@Profile("local-mock", "local-dev")
class OppstartLoggConfig {
    private val logger = LoggerFactory.getLogger(OppstartLoggConfig::class.java)

    @Bean
    fun oppstartMelding(environment: Environment) =
        ApplicationRunner {
            val port = environment.getProperty("local.server.port") ?: environment.getProperty("server.port", "8080")
            val profiler = environment.activeProfiles.joinToString(", ")
            val appNavn = environment.getProperty("spring.application.name", "gjenlevende-bs-sak")
            val prosessering = environment.getProperty("prosessering.continuousRunning.enabled", "true")

            logger.info("")
            logger.info("╭───────────────────────────────────────────────────────────────╮")
            logger.info("│                                                               │")
            logger.info("│  ✓ $appNavn startet                                 │")
            logger.info("│                                                               │")
            logger.info("├───────────────────────────────────────────────────────────────┤")
            logger.info("│  Profiler:      ${profiler.padEnd(46)}│")
            logger.info("│  Port:          ${port.padEnd(46)}│")
            logger.info("│  Prosessering:  ${prosessering.padEnd(46)}│")
            logger.info("│                                                               │")
            logger.info("│  Endepunkter:                                                 │")
            logger.info("│    → API:       ${"http://localhost:$port".padEnd(46)}│")
            logger.info("│    → Swagger:   ${"http://localhost:$port/swagger-ui.html".padEnd(46)}│")
            logger.info("│    → Health:    ${"http://localhost:$port/internal/health".padEnd(46)}│")
            logger.info("│                                                               │")
            logger.info("├───────────────────────────────────────────────────────────────┤")
            logger.info("│  For verbose logging: legg til 'verbose' profil               │")
            logger.info("╰───────────────────────────────────────────────────────────────╯")
            logger.info("")
        }
}
