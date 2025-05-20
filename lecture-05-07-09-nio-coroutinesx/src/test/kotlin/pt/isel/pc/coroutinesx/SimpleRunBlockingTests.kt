package pt.isel.pc.coroutinesx

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
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
        runBlocking(
            EmptyCoroutineContext // redundant
                    + MyCoroutineName("TestCoroutine")+ MyDispatcher()) {
          
            logger.info("Hello World from main coroutine")
            showContext()
            delay(2000)
            logger.info("Bye from main coroutine")
           
        }
        
        logger.info("test done")
    }
    
    @Test
    fun `multiple child coroutines on runBlocking`() {
        runBlocking {
            val job1 = launch {
                logger.info("start child coroutine 1")
                showContext()
                delay(2000)
                logger.info("end child coroutine 1")
            }
            val job2 = launch {
                logger.info("start child coroutine 2")
                delay(3000)
                showContext()
                logger.info("end child coroutine 2")
            }
            delay(500)
            logger.info("job1 state=${getJobState(job1)}")
            job1.join()
            logger.info("job1 state=${getJobState(job1)}")
            logger.info("main coroutine")
            delay(100)
           
        }
        logger.info("test done")
    }
    
   
    
    @Test
    fun `runBlocking cancelling child job`() {
        var job1 : Job? = null
        var job2 : Job? = null
        var job3 : Job? = null
        var mainJob : Job? = null
        try {
            runBlocking {
                logger.info("main  coroutine context:")
                showContext()
                mainJob = coroutineContext.job
                job1 = launch() {
                   
                    logger.info("first child context:")
                    showContext()
                    logger.info("start first child coroutine")
                    job3 = launch {
                        logger.info("first grand child context:")
                        showContext()
                        logger.info("start first grand child coroutine")
                        delay(2000)
                    }
                  
                    delay(500)
                    throw Error("fatal error on child 1")
                    logger.info("end first child coroutine")
                }
                
                job2 = launch {
                    logger.info("second child context:")
                    showContext()
                    logger.info("start second child coroutine")
                    delay(1500)
                    logger.info("end second child  coroutine")
                }
                
             
              
                delay(1000)
               // job1.cancel()
                
                logger.info("end main coroutine")
             
            }
        }
        catch(e: Throwable) {
            logger.info("error on runBlocking: $e")
        }
        if (mainJob != null) logger.info("mainJob state: ${getJobState(mainJob!!)}")
        if (job1 != null) logger.info("job1 state: ${getJobState(job1!!)}")
        if (job2 != null) logger.info("job2 state: ${getJobState(job2!!)}")
        if (job3 != null) logger.info("job3 state: ${getJobState(job3!!)}")
        logger.info("test done")
    }
    
    @Test
    fun `runBlocking job error on child`() {
        var job1 : Job? = null
        var job2 : Job? = null
        
        try {
            runBlocking {
                job1 = launch {
                    logger.info("start first child coroutine")
                    delay(500)
                    throw Error("fatal error on child 1")
                }
                
                job2 = launch {
                    logger.info("start second child coroutine")
                    delay(1000)
                    logger.info("end second child  coroutine")
                }
                
                logger.info("main coroutine")
                
            }
        }
        catch(e: Throwable) {
            logger.info("error on runBlocking: $e")
        }
        if (job1 != null) logger.info("job1 state: ${getJobState(job1!!)}")
        if (job2 != null) logger.info("job2 state: ${getJobState(job2!!)}")
        logger.info("test done")
    }
}