package no.nav.gjenlevende.bs.sak.security

enum class Rolle(
    val beskrivelse: String,
) {
    SAKSBEHANDLER("Kan saksbehandle i saksbehandler-løsningen"),
    BESLUTTER("Kan fatte beslutninger i på saker i saksbehandling-løsningen"),
    VEILEDER("Kan veilede og gi støtte i saksbehandling-løsningen"),
    ;

    fun authority(): String = "ROLE_$name"

    companion object {
        val AZURE_GRUPPE_TIL_ROLLE: Map<String, Set<Rolle>> =
            mapOf(
                "5357fbfa-de25-4d23-86a6-f67caf8ddd63" to setOf(SAKSBEHANDLER),
                "fda781b0-b82c-4049-919d-3b05623f05fb" to setOf(BESLUTTER),
                "0291bb72-71fa-4a35-9947-ea7b73f09ab8" to setOf(VEILEDER),
            )

        fun fraAzureGrupper(gruppeIder: List<String>): Set<Rolle> =
            gruppeIder
                .flatMap { gruppeId -> AZURE_GRUPPE_TIL_ROLLE[gruppeId] ?: emptySet() }
                .toSet()
    }
}
