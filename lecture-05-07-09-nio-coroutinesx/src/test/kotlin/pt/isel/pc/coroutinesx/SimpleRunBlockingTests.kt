package pt.isel.pc.coroutinesx

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import pt.isel.pc.nio.coroutinesx.MyCoroutineName
import pt.isel.pc.nio.coroutinesx.dispatchers.MyDispatcher
import pt.isel.pc.nio.coroutinesx.showContext
import pt.isel.pc.nio.coroutinesx.getJobState
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleRunBlockingTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    @Test
    fun `first runBlocking builder test`() {
        // add context
        runBlocking  {
            logger.info("start coroutine")
            showContext()
        }
    }
    
    @Test
    fun `multiple child coroutines on runBlocking`() {
        runBlocking {
            launch {
                logger.info("start first child coroutine")
                delay(500)
                logger.info("end first child  coroutine")
            }
       
            logger.info("main coroutine")
            delay(100)
           
        }
        logger.info("test done")
    }
    
    @Test
    fun `runBlocking job waiting for single child coroutines`() {
        runBlocking {
            val job1 = launch {
                logger.info("start first child coroutine")
                delay(500)
                logger.info("end first child  coroutine")
            }
            
            val job2 = launch {
                logger.info("start second child coroutine")
                delay(1000)
                logger.info("end second child  coroutine")
            }
            
            logger.info("main coroutine")
            job1.join()
            job2.join()
            logger.info("after join")
            logger.info("job1 state: ${getJobState(job1)}")
            logger.info("job2 state: ${getJobState(job2)}")
        }
        logger.info("test done")
    }
    
    @Test
    fun `runBlocking job error on child`() {
        TODO()
    }
}