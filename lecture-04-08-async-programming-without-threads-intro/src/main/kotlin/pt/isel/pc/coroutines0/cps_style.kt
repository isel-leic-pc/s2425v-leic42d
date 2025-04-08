package pt.isel.pc

import mu.KotlinLogging

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.*

private val logger = KotlinLogging.logger {}

private val schedExecutor = Executors.newSingleThreadScheduledExecutor()

private suspend fun delay2(millis: Long) {
    suspendCoroutine<Unit> {
        cont ->

        logger.info("in delay start")
        schedExecutor.schedule({
            logger.info("in delay end, cont = $cont")
            logger.info("in delay end, context = ${cont.context}")

            cont.resume(Unit)
            logger.info("after resume in delay end")
        }, millis, TimeUnit.MILLISECONDS)


        }
}

private suspend fun  fs(msg: String, millis: Long) : Long {
    logger.info("start fs with $msg")
    //delay2(millis)
    logger.info("end fs")
    return 23L
}

val fsn = ::fs as (String, Long, Continuation<Long>) -> Any

fun fsAsNormal() {
   
    val cdl = CountDownLatch(1)

    val res = fsn("hello", 2000, object : Continuation<Long> {
        override val context: CoroutineContext
            get() = EmptyCoroutineContext

        override fun resumeWith(result: Result<Long>) {
            logger.info("result = $result")
            cdl.countDown()
        }
    })
    logger.info("res=$res")
    cdl.await()
}

private fun main() {
    val code : suspend () -> Unit = {
        fsAsNormal()
    }
    
    val res = code.startCoroutine( object : Continuation<Unit> {
        override val context: CoroutineContext
            get() = EmptyCoroutineContext
        
        override fun resumeWith(result: Result<Unit>) {
            logger.info("result = $result")
        }
    })
    
    logger.info("terminate")
}