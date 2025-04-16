package pt.isel.pc.coroutines1

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.assertFails
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class ThreadPoolExecutorTests {
    private val logger = KotlinLogging.logger {}
    
    @Test
    fun `await threadpool termination blocking and suspend`()  {
        val executor = ThreadPoolExecutor(1, 1.minutes)
        val cdl = CountDownLatch(1)
        // here we are using a Continuation constructor function to avoid
        // implement the Continuation interface
        val cont =  Continuation<Unit>(EmptyCoroutineContext) {
            println("done")
            cdl.countDown()
        }
        executor.execute(cont)
        
        cdl.await(1000, TimeUnit.MILLISECONDS)
        
        val completion = object : Continuation<Unit> {
            override val context: CoroutineContext
                get() = EmptyCoroutineContext
            
            override fun resumeWith(result: Result<Unit>) {
                logger.info("result = $result")
            }
        }
      
        val testThread = Thread {
           
            executor.awaitTermination(Duration.INFINITE)
        }
        testThread.start()
        
        executor.shutdown()
        testThread.join(1000)
        assertFalse(testThread.isAlive)
        logger.info("await done")
    }
}