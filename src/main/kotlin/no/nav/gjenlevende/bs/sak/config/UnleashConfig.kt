package no.nav.gjenlevende.bs.sak.config

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class UnleashConfig {
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
        val config =
            UnleashConfig
                .builder()
                .appName(appName)
                .instanceId(appName)
                .unleashAPI(unleashUrl)
                .apiKey(unleashToken)
                .environment(environment)
                .build()

        return DefaultUnleash(config)
    }
}
