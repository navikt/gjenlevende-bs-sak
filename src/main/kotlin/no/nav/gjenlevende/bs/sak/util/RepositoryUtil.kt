package no.nav.gjenlevende.bs.sak.util

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull

inline fun <reified T : Any, ID : Any> CrudRepository<T, ID>.findByIdOrThrow(id: ID): T = findByIdOrNull(id) ?: throw IllegalStateException("Finner ikke ${T::class.simpleName} med id=$id")
