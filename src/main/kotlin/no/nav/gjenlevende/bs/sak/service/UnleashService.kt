package no.nav.gjenlevende.bs.sak.service

import io.getunleash.Unleash
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UnleashService(
    private val unleash: Unleash,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getFeatureToggles(): Map<String, Boolean> {
        val toggleNames = FeatureToggle.getAllToggleNames()
        logger.info("Henter ${toggleNames.size} feature toggles fra Unleash")

        return toggleNames.associateWith { toggleName ->
            val enabled = unleash.isEnabled(toggleName)
            logger.debug("Toggle '$toggleName' er ${if (enabled) "aktivert" else "deaktivert"}")
            enabled
        }
    }
}
