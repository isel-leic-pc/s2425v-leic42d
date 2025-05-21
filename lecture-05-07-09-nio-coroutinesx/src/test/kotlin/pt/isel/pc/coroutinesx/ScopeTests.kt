package pt.isel.pc.coroutinesx

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals

import pt.isel.pc.nio.coroutinesx.getJobState
import pt.isel.pc.nio.coroutinesx.showContext
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test


class ScopeTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    
    @Test
    fun `first scope using completable job test`() {
         val parentJob = Job()
         val scope = CoroutineScope(parentJob)
        
         val job1 = scope.launch {
             logger.info("start job1")
             showContext()
             delay(2000)
             logger.info("end job1")
            
        }
        
        val job2 = scope.launch {
            logger.info("start job2")
            showContext()
            delay(3000)
            logger.info("end job2")
        }
        
        runBlocking {
            delay(100)
            logger.info("${job1.parent}")
            parentJob.complete()
            parentJob.join()
            logger.info("job1 state on end: ${getJobState(job1)}")
            logger.info("job2 state on end: ${getJobState(job2)}")
            logger.info("parent state on end: ${getJobState(parentJob)}")
            logger.info("scope terminated")
        }
        logger.info("test done")
    }
    
    @Test
    fun `cancel job on scope using completable job test`() {
        val parentJob = Job()
        val scope = CoroutineScope(parentJob)
        
        val job1 = scope.launch {
            logger.info("start job1")
            showContext()
            delay(2000)
            logger.info("end job1")
          
        }
        
        val job2 = scope.launch {
            logger.info("start job2")
            showContext()
            delay(3000)
            logger.info("end job2")
        }
        
        runBlocking {
            delay(100)
            job1.cancel()
          
            parentJob.complete()
            parentJob.join()
            logger.info("job1 state on end: ${getJobState(job1)}")
            logger.info("job2 state on end: ${getJobState(job2)}")
            logger.info("parent state on end: ${getJobState(parentJob)}")
        }
        
    }
    
    
    @Test
    fun `throw exception on job using completable job test`() {
        val parentJob = Job()
        val excHandler = CoroutineExceptionHandler {
                ctx, err ->
            logger.info("in error $err context:")
            showContext(ctx)
        }
        val scope = CoroutineScope(parentJob + excHandler)
        
        val job1 = scope.launch {
            logger.info("start job1")
            showContext()
            delay(1000)
            
            throw Error("fatal error on job1")
        }
        
        val job2 = scope.launch {
            logger.info("start job2")
            showContext()
            delay(3000)
            logger.info("end job2")
        }
        
        runBlocking {
            delay(100)
            logger.info("${job1.parent}")
            parentJob.complete()
            parentJob.join()
            logger.info("job1 state on end: ${getJobState(job1)}")
            logger.info("job2 state on end: ${getJobState(job2)}")
            logger.info("parent state on end: ${getJobState(parentJob)}")
            
        }
        logger.info("test done")
        
    }
    
    @Test
    fun `throw exception on job using supervisor job test`() {
        val parentJob = SupervisorJob()
        val excHandler = CoroutineExceptionHandler {
            ctx, err ->
            logger.info("in error $err context:")
            showContext(ctx)
        }
        val scope = CoroutineScope(parentJob+ excHandler)
        
        val job1 = scope.launch {
            logger.info("start job1")
            showContext()
            delay(1000)
            throw Error("fatal error on job1")
        }
        
        val job2 = scope.launch {
            logger.info("start job2")
            showContext()
            delay(3000)
            logger.info("end job2")
        }
        
        runBlocking {
            delay(100)
            logger.info("${job1.parent}")
            parentJob.complete()
            parentJob.join()
            logger.info("job1 state on end: ${getJobState(job1)}")
            logger.info("job2 state on end: ${getJobState(job2)}")
            logger.info("parent state on end: ${getJobState(parentJob)}")
        }
        
        logger.info("test done")
        
    }
    
}