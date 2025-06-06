package pt.isel.pc.coroutines1

import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = KotlinLogging.logger {}
private val scheduler = Executors.newSingleThreadScheduledExecutor()

suspend fun delay(millis: Long) {
    suspendCoroutine<Unit> { cont ->
        // just to check a non suspend situation
        if (millis == 0L)
            cont.resume(Unit)
        else {
            logger.info("in delay start")
            
            scheduler.schedule({
                logger.info("in delay end, cont = $cont")
                logger.info("in delay end, context = ${cont.context}")
                cont.resume(Unit)
                // continue on the continuation thread pool (not implemented)
                // pool.execute(cont)
                logger.info("after resume in delay end")
            }, millis, TimeUnit.MILLISECONDS)
        }
    }
}

class CoroutineName(val name: String) : CoroutineContext.Element {
    companion object Key: CoroutineContext.Key<CoroutineName>
    override val key: CoroutineContext.Key<*>
        get() = CoroutineName
    
    override fun toString(): String = "CoroutineName($name)"
}
