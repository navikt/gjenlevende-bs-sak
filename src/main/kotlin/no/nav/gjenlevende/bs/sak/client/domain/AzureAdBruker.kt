package no.nav.gjenlevende.bs.sak.client.domain

import java.util.UUID

data class AzureAdBruker(
    val id: UUID,
    val onPremisesSamAccountName: String,
    val userPrincipalName: String,
    val givenName: String,
    val surname: String,
    val streetAddress: String,
    val city: String,
)
