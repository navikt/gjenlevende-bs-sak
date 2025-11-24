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
                "8df38a8c-6b34-49d7-b837-cefb153a03e8" to setOf(SAKSBEHANDLER),
                "f9837eec-8d85-4f61-b89e-677e168fdf2f" to setOf(BESLUTTER),
                "8c98e41f-4370-46e6-998a-2190c7f935bc" to setOf(VEILEDER),
            )

        fun fraAzureGrupper(gruppeIder: List<String>): Set<Rolle> =
            gruppeIder
                .flatMap { gruppeId -> AZURE_GRUPPE_TIL_ROLLE[gruppeId] ?: emptySet() }
                .toSet()
    }
}
