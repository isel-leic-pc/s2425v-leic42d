package pt.isel.pc.coroutinesx

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import mu.KotlinLogging.logger
import kotlin.coroutines.Continuation
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class CancellableSuspensionsTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    
    @Test
    fun `effect of normal suspend (non cancellable) on job cancellation`() {
        val parentJob = SupervisorJob()
        val scope = CoroutineScope(parentJob)
        var continuation : Continuation<Unit>? = null
        
        val job1 = scope.launch {
            showContext()
            try {
                suspendCoroutine { cont ->
                    continuation = cont
                }
            }
            catch(e: CancellationException) {
                logger.info("cancellation exception")
            }
            logger.info("after cancellation")
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
            logger.info("${job1.parent}")
            parentJob.complete()
            try {
                withTimeout(4.seconds) {
                    parentJob.join()
                }
            }
            catch(e: CancellationException) {
            
            }
            logger.info("job1 state on end: ${getJobState(job1)}")
            logger.info("job2 state on end: ${getJobState(job2)}")
            logger.info("parent state on end: ${getJobState(parentJob)}")
        }
    }
    
    @Test
    fun `effect of cancellable suspend  on job cancellation`() {
        val parentJob = SupervisorJob()
        val scope = CoroutineScope(parentJob)
        var continuation : CancellableContinuation<Unit>? = null
        val job1 = scope.launch {
            showContext()
            try {
                suspendCancellableCoroutine { cont ->
                    continuation = cont
                    
                    cont.invokeOnCancellation {
                        logger.info("on cancellation")
                    }
                }
            }
            catch(e: CancellationException) {
                logger.info("cancellation exception")
            }
            logger.info("after cancellation")
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
            withTimeout(4.seconds) {
                parentJob.join()
            }
            logger.info("job1 state on end: ${getJobState(job1)}")
            logger.info("job2 state on end: ${getJobState(job2)}")
            logger.info("parent state on end: ${getJobState(parentJob)}")
            
            logger.info("${getJobState(job1)}")
        }
    }
}