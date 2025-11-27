package no.nav.gjenlevende.bs.sak.infrastruktur.exception

import org.springframework.http.HttpStatus

class ApiFeil(
    val feilmelding: String,
    val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
) : RuntimeException(feilmelding)
