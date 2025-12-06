package no.nav.gjenlevende.bs.sak.infrastruktur.exception

import no.nav.familie.kontrakter.felles.Ressurs
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.servlet.resource.NoResourceFoundException

@ControllerAdvice(basePackages = ["no.nav.gjenlevende.bs.sak"])
class ApiExceptionHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(ApiFeil::class)
    fun handleApiFeil(feil: ApiFeil): ResponseEntity<FeilResponse> {
        logger.warn("ApiFeil: ${feil.feilmelding}", feil)
        return ResponseEntity
            .status(feil.httpStatus)
            .body(FeilResponse(feil.feilmelding, feil.httpStatus.value()))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<FeilResponse> {
        logger.warn("IllegalArgumentException: ${e.message}", e)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(FeilResponse(e.message ?: "Ugyldig request", HttpStatus.BAD_REQUEST.value()))
    }

    @ExceptionHandler(WebClientResponseException::class)
    fun handleWebClientResponseException(e: WebClientResponseException): ResponseEntity<FeilResponse> {
        val feilmelding =
            try {
                e.responseBodyAsString
            } catch (ex: Exception) {
                "Feil fra downstream tjeneste"
            }

        logger.warn("WebClientResponseException: ${e.statusCode} - $feilmelding")
        return ResponseEntity
            .status(e.statusCode)
            .body(FeilResponse(feilmelding, e.statusCode.value()))
    }

    @ExceptionHandler(ManglerTilgang::class)
    fun handleThrowable(manglerTilgang: ManglerTilgang): ResponseEntity<Ressurs<Nothing>> {
        logger.warn("En håndtert tilgangsfeil har oppstått")
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(
                Ressurs(
                    data = null,
                    status = Ressurs.Status.IKKE_TILGANG,
                    frontendFeilmelding = manglerTilgang.frontendFeilmelding,
                    melding = manglerTilgang.melding,
                    stacktrace = null,
                ),
            )
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(e: NoResourceFoundException): ResponseEntity<FeilResponse> {
        // Re-throw sånn at Spring kan håndtere sin egne resource not found exceptions
        // Dette fikser unødvendig støy i loggene fra actuator-/interne endepunktene
        throw e
    }
}

data class FeilResponse(
    val melding: String,
    val status: Int,
)
