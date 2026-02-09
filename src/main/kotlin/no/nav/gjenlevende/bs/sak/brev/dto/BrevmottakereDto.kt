package no.nav.gjenlevende.bs.sak.brev.dto

import no.nav.gjenlevende.bs.sak.brev.domain.Brevmottaker

data class BrevmottakereDto(
    val brevmottakere: List<Brevmottaker>,
)
