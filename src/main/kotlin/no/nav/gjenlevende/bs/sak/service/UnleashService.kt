package no.nav.gjenlevende.bs.sak.service

import io.getunleash.Unleash
import org.springframework.stereotype.Service

@Service
class UnleashService(
    private val unleash: Unleash,
) {
    fun getAllToggles(): Map<String, Boolean> =
        unleash
            .more()
            .getFeatureToggleNames()
            .associateWith { toggleName -> unleash.isEnabled(toggleName) }
}
