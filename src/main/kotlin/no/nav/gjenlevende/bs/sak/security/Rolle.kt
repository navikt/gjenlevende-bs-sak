package no.nav.gjenlevende.bs.sak.security

enum class Rolle(
    val beskrivelse: String,
) {
    SAKSBEHANDLER("Kan saksbehandle i saksbehandler-løsningen"),
    BESLUTTER("Kan fatte beslutninger i på saker i saksbehandling-løsningen"),
    FORVALTER("Kan gjøre forvalter ting idk"),
    ;

    fun authority(): String = "ROLE_$name"

    companion object {
        val AZURE_GRUPPE_TIL_ROLLE: Map<String, Set<Rolle>> =
            mapOf(
                "ee5e0b5e-454c-4612-b931-1fe363df7c2c" to setOf(SAKSBEHANDLER),
                "01166863-22f1-4e16-9785-d7a05a22df74" to setOf(BESLUTTER),
                "59865891-62a0-4fe3-b282-2e38210d1fbb" to setOf(FORVALTER),
            )

        fun fraAzureGrupper(gruppeIder: List<String>): Set<Rolle> =
            gruppeIder
                .flatMap { gruppeId -> AZURE_GRUPPE_TIL_ROLLE[gruppeId] ?: emptySet() }
                .toSet()
    }
}
