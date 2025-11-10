package no.nav.gjenlevende.bs.sak.security

/**
 * Applikasjonsroller for tilgangskontroll.
 *
 * Roller er mappet fra Azure AD grupper og brukes med @PreAuthorize annotasjoner.
 * Merk: Rollene er separate - BESLUTTER kan ikke automatisk behandle saker.
 * For å både behandle og beslutte kreves begge rollene (separation of duties).
 */
enum class Rolle(
    val beskrivelse: String,
) {
    SAKSBEHANDLER("Kan saksbehandle i saksbehandler-løsningen"),
    BESLUTTER("Kan fatte beslutninger i på saker i saksbehandling-løsningen"),
    FORVALTER("Kan gjøre forvalter ting idk"),
    ;

    /**
     * Returnerer rollen som Spring Security authority (med ROLE_ prefix).
     */
    fun authority(): String = "ROLE_$name"

    companion object {
        /**
         * Mapping fra Azure AD gruppe-ID til applikasjonsroller.
         * Disse gruppe-IDene hentes fra .nais/dev.yaml
         *
         * Merk: En gruppe kan gi flere roller hvis nødvendig.
         * Brukere som skal ha både SAKSBEHANDLER og BESLUTTER må være medlem
         * av minst én gruppe som gir hver av rollene.
         *
         * TODO: Husk å injecte disse basert på miljø.
         */
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
