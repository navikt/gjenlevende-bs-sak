package no.nav.gjenlevende.bs.sak.unleash

enum class FeatureToggle(
    val toggleName: String,
) {
    TEST_SETUP("gjenlevende_frontend__test_setup"),
    TOGGLE_TILGANGSMASKIN_I_DEV("gjenlevende_backend_toggle_tilgangsmaskin_i_dev"),
    HENLEGG_BEHANDLING("gjenlevende-bs-sak-henlegg-behandling"),
    ;

    companion object {
        fun hentAlleFeatureToggleNavn(): List<String> = entries.map { it.toggleName }
    }
}
