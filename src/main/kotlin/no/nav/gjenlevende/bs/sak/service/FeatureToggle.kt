package no.nav.gjenlevende.bs.sak.service

enum class FeatureToggle(
    val toggleName: String,
) {
    TEST_SETUP("gjenlevende_frontend__test_setup"),
    ;

    companion object {
        fun getAllToggleNames(): List<String> = entries.map { it.toggleName }
    }
}
