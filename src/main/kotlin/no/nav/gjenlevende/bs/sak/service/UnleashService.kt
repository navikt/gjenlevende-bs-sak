package no.nav.gjenlevende.bs.sak.service

import io.getunleash.Unleash
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UnleashService(
    private val unleash: Unleash,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getAllToggles(): Map<String, Boolean> {
        val toggleNames = unleash.more().getFeatureToggleNames()
        logger.info("Henter alle toggles. Antall funnet: ${toggleNames.size}")
        logger.debug("Toggle-navn: $toggleNames")

        return toggleNames.associateWith { toggleName ->
            val enabled = unleash.isEnabled(toggleName)
            logger.debug("Toggle '$toggleName' er ${if (enabled) "aktivert" else "deaktivert"}")
            enabled
        }
    }

    fun isEnabled(toggleName: String): Boolean {
        val enabled = unleash.isEnabled(toggleName)
        logger.debug("Sjekker toggle '$toggleName': $enabled")
        return enabled
    }

    fun getTestSetupToggle(): Boolean = isEnabled("gjenlevende_frontend__test_setup")
}
