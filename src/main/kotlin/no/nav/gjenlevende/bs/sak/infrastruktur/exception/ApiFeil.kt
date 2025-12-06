package no.nav.gjenlevende.bs.sak.infrastruktur.exception

import org.springframework.http.HttpStatus

class ApiFeil(
    val feilmelding: String,
    val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
) : RuntimeException(feilmelding)

class Feil(
    message: String,
    val frontendFeilmelding: String? = null,
    val httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    throwable: Throwable? = null,
) : RuntimeException(message, throwable) {
    constructor(message: String, throwable: Throwable?, httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR) :
        this(message, null, httpStatus, throwable)
}

class ManglerTilgang(
    val melding: String,
    val frontendFeilmelding: String,
) : RuntimeException(melding)
