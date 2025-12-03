package no.nav.gjenlevende.bs.sak.infrastruktur.exception

import org.springframework.http.HttpStatus
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class ApiFeil(
    val feilmelding: String,
    val httpStatus: HttpStatus = HttpStatus.BAD_REQUEST,
) : RuntimeException(feilmelding)

inline fun feilHvisIkke(
    boolean: Boolean,
    httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    lazyMessage: () -> String,
) {
    feilHvis(!boolean, httpStatus) { lazyMessage() }
}

@OptIn(ExperimentalContracts::class)
inline fun feilHvis(
    boolean: Boolean,
    httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    lazyMessage: () -> String,
) {
    contract {
        returns() implies !boolean
    }
    if (boolean) {
        throw Feil(message = lazyMessage(), frontendFeilmelding = lazyMessage(), httpStatus)
    }
}

class Feil(
    message: String,
    val frontendFeilmelding: String? = null,
    val httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    throwable: Throwable? = null,
) : RuntimeException(message, throwable) {
    constructor(message: String, throwable: Throwable?, httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR) :
        this(message, null, httpStatus, throwable)
}
