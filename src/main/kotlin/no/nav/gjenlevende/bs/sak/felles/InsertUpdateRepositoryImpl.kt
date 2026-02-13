package no.nav.gjenlevende.bs.sak.felles

import no.nav.gjenlevende.bs.sak.felles.sikkerhet.SikkerhetContext
import no.nav.gjenlevende.bs.sak.felles.sporbar.Sporing
import org.springframework.data.jdbc.core.JdbcAggregateOperations
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

@Component
open class InsertUpdateRepositoryImpl<T : Any>(
    private val entityOperations: JdbcAggregateOperations,
) : InsertUpdateRepository<T> {
    @Transactional
    override fun insert(t: T): T = entityOperations.insert(t)

    @Transactional
    override fun insertAll(list: List<T>): List<T> = list.map(this::insert)

    @Transactional
    override fun update(t: T): T = entityOperations.update(oppdaterSporingVedEndring(t))

    @Transactional
    override fun updateAll(list: List<T>): List<T> = list.map(this::update)

    private fun oppdaterSporingVedEndring(entity: T): T {
        val kClass = entity::class
        val sporingProperty =
            kClass.memberProperties.find { it.name == "sporing" && it.returnType.classifier == Sporing::class }
                ?: return entity

        @Suppress("UNCHECKED_CAST")
        val sporing = (sporingProperty as KProperty1<T, Sporing>).get(entity)
        val oppdatertSporing =
            sporing.copy(
                endretAv = SikkerhetContext.hentSaksbehandlerEllerSystembruker(),
                endretTid = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            )

        val copyFunction = kClass.memberFunctions.find { it.name == "copy" } ?: return entity
        val sporingParam = copyFunction.parameters.find { it.name == "sporing" } ?: return entity

        @Suppress("UNCHECKED_CAST")
        return copyFunction.callBy(
            mapOf(
                copyFunction.parameters.first { it.kind == KParameter.Kind.INSTANCE } to entity,
                sporingParam to oppdatertSporing,
            ),
        ) as T
    }
}
