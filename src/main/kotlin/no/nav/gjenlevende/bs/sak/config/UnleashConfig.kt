package no.nav.gjenlevende.bs.sak.config

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class UnleashConfig {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${unleash.url}")
    private lateinit var unleashUrl: String

    @Value("\${unleash.token}")
    private lateinit var unleashToken: String

    @Value("\${unleash.app-name}")
    private lateinit var appName: String

    @Value("\${unleash.environment}")
    private lateinit var environment: String

    @Bean
    open fun unleash(): Unleash {
        logger.info("Konfigurerer Unleash med URL: $unleashUrl, App: $appName, Miljø: $environment")

        val config =
            UnleashConfig
                .builder()
                .appName(appName)
                .instanceId(appName)
                .unleashAPI(unleashUrl)
                .apiKey(unleashToken)
                .environment(environment)
                .synchronousFetchOnInitialisation(true)
                .build()

        val unleash = DefaultUnleash(config)

        logger.info("Unleash initialisert. Antall toggles: ${unleash.more().getFeatureToggleNames().size}")

        return unleash
    }
}
