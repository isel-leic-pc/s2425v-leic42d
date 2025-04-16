package pt.isel.pc.coroutines1

import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.*

class CoroutinesBuiltinTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
   
    
    // a suspend funtion no reaaly needes to be suspend
    // just to observe the compled signature
    suspend fun f1() {
        logger.info("Hello from coroutine")
    }
    
    @Test
    fun `create a simple coroutine`() {
        
        // A simple Continuation implementation
        val completion = object : Continuation<Unit> {
            override val context: CoroutineContext
                get() = EmptyCoroutineContext
            
            override fun resumeWith(result: Result<Unit>) {
                logger.info("result = $result")
            }
        }
        
        // start f1 as a coroutine
        ::f1.startCoroutine(completion)
         
        logger.info("done")
        
    }
    
    suspend fun f2(millis: Long) {
        logger.info("Start suspend")
        
        // suspend forever since the continuation (cont) is
        // never resumed
        var res = suspendCoroutine<Unit> {
            cont ->
        }
        logger.info("End suspend with result $res")
    }
    
    @Test
    fun `coroutine with a suspension point`() {
        val completion = object : Continuation<Unit> {
            override val context: CoroutineContext
                get() = EmptyCoroutineContext
            
            override fun resumeWith(result: Result<Unit>) {
                logger.info("result = $result")
            }
        }
        
        // note thar startCoroutine returns on the first suspension point
        var res = suspend { f2(2000) }.startCoroutine(completion)
        logger.info("startCoroutine return $res")
       
        println("done")
    }
    
    private suspend fun  func1() : Long {
        // use the coroutineContext to retrieve the given coroutine name
        val name = coroutineContext.get(CoroutineName)?.name
        logger.info("phase 1 on $name");
//  a bad implementation of delay
//        suspendCoroutine<Long> {
//            cont ->
//             Thread {
//                 Thread.sleep(2000)
//                 cont.resume(2000)
//             }
//             .start()
//        }
        delay(2000)
        logger.info("phase 2 on $name")
 
        delay(3000)
        
        logger.info("phase 3 on $name with result")
        return 1L
    }
    
    @Test
    fun `function with many suspension points`() {
        //cdl used to avoid premature test termination
        val cdl = CountDownLatch(1)
        val completion = object : Continuation<Any> {
            override val context: CoroutineContext
                get() =  CoroutineName("coroutine 1")
            
            override fun resumeWith(result: Result<Any>) {
                logger.info("result = $result")
                cdl.countDown()
            }
        }
        suspend {
            func1()
        }.startCoroutine(completion)
        
        cdl.await()
        logger.info("test done")
    }
    
    @Test
    fun `thread pool continuations executor from many coroutines`() {
        val CR_NUMBER = 5
        val cdl = CountDownLatch(CR_NUMBER)
        repeat(CR_NUMBER) {
                index->
            val completion = object :Continuation<Long> {
                override val context: CoroutineContext
                    get() = CoroutineName("coroutine ${index+1}")
                    // add a CoroutineName  name element to coroutine context
                override fun resumeWith(result: Result<Long>) {
                    logger.info("completion result: $result")
                    cdl.countDown()
                }
            }
            
            var cont = ::func1.createCoroutine(completion)
            pool.execute(cont)
            logger.info("After initial continuation resume")
        }
        cdl.await()
       
        logger.info("test done")
    }
    
    /**
     * the technique used on this test is necessary on
     * third exercise of second series
     */
    @Test
    fun `two coroutines simultaneously working on same thread`() {
        
        val continuations = mutableListOf<Continuation<Unit>>()
        
        val completion = object : Continuation<Unit> {
            override val context: CoroutineContext
                get() = EmptyCoroutineContext
            
            override fun resumeWith(result: Result<Unit>) {
                logger.info("result = $result")
            }
        }
        
        var f1 : suspend () -> Unit = {
            var teams = listOf("Benfica", "Real Madrid", "Manchester United")
            for (t in teams) {
                println(t)
                
                suspendCoroutine<Unit> {
                    cont -> continuations.addLast(cont)
                }
                println("after suspend in $t")
            }
        }
        
        var f2 : suspend () -> Unit = {
            var countries = listOf("Portugal", "Spain", "England")
            for (c in countries) {
                println(c)

                suspendCoroutine<Unit> {
                        cont -> continuations.addLast(cont)
                }
                println("after suspend in $c")
            }
        }
        
        f1.startCoroutine(completion)
        f2.startCoroutine(completion)
        
        
        while(continuations.isNotEmpty()) {
            continuations.removeFirst().resumeWith(Result.success(Unit))
        }
        
        logger.info("done")
    }
    
}