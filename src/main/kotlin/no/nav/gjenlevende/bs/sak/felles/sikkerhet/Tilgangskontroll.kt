package no.nav.gjenlevende.bs.sak.felles.sikkerhet

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Tilgangskontroll(
    val auditLogMelding: String = "",
)
