package no.nav.gjenlevende.bs.sak.infrastruktur.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ApiExceptionHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(ApiFeil::class)
    fun handleApiFeil(feil: ApiFeil): ResponseEntity<FeilResponse> {
        logger.warn("ApiFeil kastet: ${feil.feilmelding}", feil)
        return ResponseEntity
            .status(feil.httpStatus)
            .body(FeilResponse(feil.feilmelding, feil.httpStatus.value()))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<FeilResponse> {
        logger.warn("IllegalArgumentException kastet: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(FeilResponse(e.message ?: "Ugyldig forespørsel", HttpStatus.BAD_REQUEST.value()))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<FeilResponse> {
        logger.error("Uventet feil: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(FeilResponse("En uventet feil oppstod", HttpStatus.INTERNAL_SERVER_ERROR.value()))
    }
}

data class FeilResponse(
    val melding: String,
    val status: Int,
)
