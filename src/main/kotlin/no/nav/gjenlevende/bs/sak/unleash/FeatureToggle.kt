package no.nav.gjenlevende.bs.sak.unleash

enum class FeatureToggle(
    val toggleName: String,
) {
    TEST_SETUP("gjenlevende_frontend__test_setup"),
    ;

    companion object {
        fun hentAlleFeatureToggleNavn(): List<String> = entries.map { it.toggleName }
    }
}
