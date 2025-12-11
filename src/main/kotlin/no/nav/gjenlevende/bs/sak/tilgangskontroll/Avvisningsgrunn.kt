package no.nav.gjenlevende.bs.sak.tilgangskontroll

enum class Avvisningsgrunn(
    val kode: String,
    val beskrivelse: String,
) {
    AVVIST_STRENGT_FORTROLIG_ADRESSE(
        "AVVIST_STRENGT_FORTROLIG_ADRESSE",
        "Du har ikke tilgang til brukere med strengt fortrolig adresse",
    ),
    AVVIST_STRENGT_FORTROLIG_UTLAND(
        "AVVIST_STRENGT_FORTROLIG_UTLAND",
        "Du har ikke tilgang til brukere med strengt fortrolig adresse i utlandet",
    ),
    AVVIST_FORTROLIG_ADRESSE(
        "AVVIST_FORTROLIG_ADRESSE",
        "Du har ikke tilgang til brukere med fortrolig adresse",
    ),
    AVVIST_SKJERMING(
        "AVVIST_SKJERMING",
        "Du har ikke tilgang til Nav-ansatte og andre skjermede brukere",
    ),
    AVVIST_HABILITET(
        "AVVIST_HABILITET",
        "Du har ikke tilgang til data om deg selv eller dine nærstående",
    ),
    AVVIST_AVDOD(
        "AVVIST_AVDØD",
        "Du har ikke tilgang til avdøde brukere",
    ),
    AVVIST_GEOGRAFISK(
        "AVVIST_GEOGRAFISK",
        "Du har ikke tilgang til brukerens geografiske område",
    ),
    AVVIST_UKJENT_BOSTED(
        "AVVIST_UKJENT_BOSTED",
        "Du har ikke tilgang til brukere med ukjent bosted",
    ),
    AVVIST_PERSON_UTLAND(
        "AVVIST_PERSON_UTLAND",
        "Du har ikke tilgang til brukere i utlandet",
    ),
    UKJENT(
        "UKJENT",
        "Tilgang avvist av ukjent grunn",
    ),
    ;

    companion object {
        fun fraKode(kode: String?): Avvisningsgrunn? =
            kode?.let { k ->
                entries.find { it.kode == k } ?: UKJENT
            }
    }
}
