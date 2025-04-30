package pt.isel.pc.coroutines1

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine
import kotlin.test.assertFails
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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
    
    @Test
    fun `thread pool continuation executors tests`() {
        val pool = ThreadPoolExecutor(1, 1.minutes)
        
        val cdl = CountDownLatch(1)
        val suspBlock : suspend () -> Unit = {
                delay(100)
                logger.info("phase 1")
        }
    
        
        val completion = Continuation<Unit>(EmptyCoroutineContext ) {
            logger.info("completion")
            cdl.countDown()
        }
        
        val cont = suspBlock.createCoroutine(completion)
        
        pool.execute(cont)
        cdl.await()
        
        pool.shutdown()
        pool.awaitTermination(Duration.INFINITE)
      
        logger.info("test done with cont = $cont")
    }
    
    private val MAX_THREADS = 4
    private val KEEP_ALIVE = 60.seconds
    private val executor = ThreadPoolExecutor(MAX_THREADS,KEEP_ALIVE)
    
    private suspend fun yield () {
        suspendCoroutine<Unit> {
                cont ->
            // an yield operation on coroutines
            // the resume continuation on the continuation threpool
            // (notr implemented)
            executor.execute(cont)
        }
    }
    
    private suspend fun f1( counter: AtomicInteger) {
        logger.info("first increment")
        counter.incrementAndGet()
        yield()
        
        logger.info("second increment")
        counter.incrementAndGet()
        yield()
        
        logger.info("third increment")
        counter.incrementAndGet()
    }
    
    @Test
    fun `thread pool executor executing corrotine continuations`() {
        var count = 0
        val cdl = CountDownLatch(1)
        
        val completion = object : Continuation<Unit> {
            override val context: CoroutineContext
                get() = EmptyCoroutineContext
            
            override fun resumeWith(result: Result<Unit>) {
                logger.info("result = $result")
                cdl.countDown()
            }
        }
        
        val counter = AtomicInteger()
        
        suspend { f1(counter) }.startCoroutine(completion)
        logger.info("coroutine started")
        // executor.execute(cont)
        
        cdl.await()
        logger.info("counter = ${counter.get()}")
        
    }
}