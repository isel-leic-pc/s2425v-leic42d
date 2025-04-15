package pt.isel.pc.coroutines0

import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import java.lang.Thread.startVirtualThread
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.*

private val logger = KotlinLogging.logger {}
private val scheduler = Executors.newSingleThreadScheduledExecutor()

suspend fun myDelay(millis: Long) {
    suspendCoroutine<Unit> { cont ->
        scheduler.schedule({
            cont.resume(Unit)
        },millis, TimeUnit.MILLISECONDS);
    }
}

fun f1() {
    println("Hello from coroutine")
}

suspend fun f2(millis: Long) {
    logger.info("Start delay")
    myDelay(millis)
    logger.info("End delay")
}

class CoroutinesBuiltinTests {
    
    @Test
    fun `create a simple coroutine`() {
        
        val completion = object : Continuation<Unit> {
            override val context: CoroutineContext
                get() = EmptyCoroutineContext
            
            override fun resumeWith(result: Result<Unit>) {
                logger.info("result = $result")
            }
        }
        
        suspend { f1() }.startCoroutine(completion)
         
        println("done")
        
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
        
        suspend { f2(2000) }.startCoroutine(completion)
        
        sleep(3000)
        println("done")
    }
    
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
    
    suspend fun f3() {
        logger.info("f3 start");
        myDelay(2000);
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
    
    @Test
    fun `deep suspension point test`() {
        
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
}