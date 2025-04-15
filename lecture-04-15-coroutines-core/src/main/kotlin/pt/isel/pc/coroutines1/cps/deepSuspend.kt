package pt.isel.pc.coroutines1.cps

import mu.KotlinLogging
import pt.isel.pc.coroutines1.delay
import java.lang.Thread.sleep
import kotlin.coroutines.*

private val logger = KotlinLogging.logger {}

suspend fun f3() {
    logger.info("f3 start");
    delay(2000);
    logger.info("f3 end");
}

suspend fun f2() {
    logger.info("f2 start");
    f3();
    logger.info("f2 end");
}

suspend fun f1() {
    logger.info("f1 start");
    f2();
    logger.info("f1 end");
}


fun main() {
    
    val continuations = mutableListOf<Continuation<Unit>>()
    
    val completion = object : Continuation<Unit> {
        override val context: CoroutineContext
            get() = EmptyCoroutineContext
        
        override fun resumeWith(result: Result<Unit>) {
            logger.info("result = $result")
        }
    }
    
    var f0 : suspend () -> Unit = {
        logger.info("f0 start");
        f1();
        logger.info("f0 end");
    }
    
    
    logger.info("create first coroutine");
    val startCompletion1 = f0.createCoroutine(completion)
    
    logger.info("create second coroutine");
    val startCompletion2 = f0.createCoroutine(completion)
    
    startCompletion1.resume(Unit)
    logger.info("first coroutine started");
    startCompletion2.resume(Unit)
    logger.info("second coroutine started");
    sleep(5000)
    logger.info("done")
}