package no.nav.gjenlevende.bs.sak.vedtak

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/vedtak")
class VedtakController(
    private val vedtakService: VedtakService,
) {
    @GetMapping("/{behandlingId}/hent-vedtak")
    fun hentVedtak(
        @PathVariable behandlingId: UUID,
    ): ResponseEntity<VedtakDto> {
        val vedtak = vedtakService.hentVedtak(behandlingId)
        return ResponseEntity.ok(vedtak?.tilDto())
    }

    @PostMapping("/{behandlingId}/lagre-vedtak")
    fun lagreVedtak(
        @PathVariable behandlingId: UUID,
        @RequestBody vedtakDto: VedtakDto,
    ): ResponseEntity<UUID> {
        vedtakService.slettVedtakHvisFinnes(behandlingId)
        vedtakService.lagreVedtak(vedtakDto = vedtakDto, behandlingId = behandlingId)
        return ResponseEntity.ok(behandlingId)
    }
}
