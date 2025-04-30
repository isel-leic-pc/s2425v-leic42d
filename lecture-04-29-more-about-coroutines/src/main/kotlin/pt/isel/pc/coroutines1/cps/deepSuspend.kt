package pt.isel.pc.coroutines1.cps

import mu.KotlinLogging
import pt.isel.pc.coroutines1.delay
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.*

/**
 * In this program we try to illustrate the execution thread
 * that occurs on suspension points.
 * Pay careful attention to the produced output.
 * After that, change the call to f3 in suspend function f2 (line 42) to f3Normal and
 * carefully observe the differences
 *
 */
private val logger = KotlinLogging.logger {}


suspend fun f3() {
    logger.info("f3 start");
    delay(2000);
    logger.info("f3 end");
}

/* this hack permits the observation of the special value COROUTINE_SUSPENDED
 * on suspension points, that is the mechanism the support the return to the
 * the execution thread to what we can call the coroutine manager (in this case the main function).
 */
val f3s = ::f3 as (cont: Continuation<Unit>) -> Any

fun f3Normal() {
    val cont = Continuation<Unit>(EmptyCoroutineContext) {
        logger.info("continuation resumed")
    }
    val res = f3s(cont);
    logger.info("f3s return $res")
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
    var cdl = CountDownLatch(2)
    val completion = object : Continuation<Unit> {
        override val context: CoroutineContext
            get() = EmptyCoroutineContext
        
        override fun resumeWith(result: Result<Unit>) {
            logger.info("result = $result")
            cdl.countDown()
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
    cdl.await()
    logger.info("done")
}