package pt.isel.pc.coroutinesx.asynchronizers

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import pt.isel.pc.coroutinesx.getJobState
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class SemaphoreCRCTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    @Test
    fun `cancel on acquire`() {
        val sem = SemaphoreCRC(1)
        runBlocking(Dispatchers.IO) {
            val job = launch {
                try {
                   sem.acquire(1)
                }
                catch(e: Exception) {
                    logger.info("coroutine terminated with exception $e")
                    logger.info("current permits in catch = ${sem.currentPermits()}")
                    
                }
              
                logger.info("current permits out of catch = ${sem.currentPermits()}")
                logger.info("start 2 seconds delay")
                try {
                    withContext(NonCancellable) {
                        delay(2.seconds)
                    }
                  
                }
                catch(e: CancellationException) {
                
                }
                logger.info("after 2 seconds delay")
                logger.info("coroutine state = ${getJobState(coroutineContext[Job]!!)}")
                
            }
         
            delay(100)
            job.cancel()
        }
    }
}