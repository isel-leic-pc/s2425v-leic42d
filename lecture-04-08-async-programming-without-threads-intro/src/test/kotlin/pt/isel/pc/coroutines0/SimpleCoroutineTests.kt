package pt.isel.pc.coroutines0

import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import kotlin.coroutines.*


class SimpleCoroutineTests {
    private val logger = KotlinLogging.logger {}
    private val schedExecutor = Executors.newSingleThreadScheduledExecutor()
    
    val completion = object : Continuation<Unit> {
        override val context: CoroutineContext
            get() = EmptyCoroutineContext
        
        override fun resumeWith(result: Result<Unit>) {
            logger.info("result = $result")
        }
    }
    
    @Test
    fun `create a simple coroutine`() {
        val code : suspend () -> Unit = {
            println("Hello from coroutine")
            println("context is $coroutineContext")
            suspendCoroutine {
            
            }
        }
        
     
        val res = code.startCoroutine(completion)
         
        println(res);
        
    }
}