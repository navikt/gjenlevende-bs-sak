package no.nav.gjenlevende.bs.sak.brev

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/brev"])
class BrevController {
    @PostMapping("/test")
    fun lagSaksbehandlerbrev(
        @RequestBody brevRequest: BrevRequest,
    ): ResponseEntity<String> = ResponseEntity.ok("OK")
}

data class BrevRequest(
    val brevMal: String,
    val fritekstBolker: List<FritekstBolk>,
)

data class FritekstBolk(
    val deltittel: String,
    val innhold: String,
)
