package pt.isel.pc.coroutinesx

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import kotlin.test.Test

class CoroutineScopeFunctionTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
   
    private suspend fun doCalc(value: Int) : Int {
        try {
            delay(1000)
            return value * 2
        }
        catch(e: Exception) {
            logger.info("doCalc terminated with error $e")
            throw e
        }
    }
    
    private suspend fun doCalcWithError(value: Int) : Int {
        try {
            delay(1000)
            throw RuntimeException("error on value calculation")
            return value * 2
        }
        catch(e: Exception) {
            logger.info("doCalc terminated with error $e")
            throw e
        }
    }
    
    private suspend fun process(value: Int) : Int = coroutineScope {
        val parentJob = coroutineContext.job
        logger.info("parent job in coroutineScope: $parentJob")
        logger.info("parent of parentJob in coroutine scope: ${parentJob.parent}")
        logger.info("context for coroutineScope:")
        showContext()
        val resultDefer = async {
            doCalc(value)
        }
        resultDefer.await()
    }
    
    private suspend fun processWithError(value: Int) : Int = coroutineScope {
        val parentJob = coroutineContext.job
        logger.info("parent job in coroutineScope: $parentJob")
        logger.info("parent of parentJob in coroutine scope: ${parentJob.parent}")
        logger.info("context for coroutineScope:")
        showContext()
        val resultDefer = async {
            doCalcWithError(value)
        }
        resultDefer.await()
    }
    
    @Test
    fun `simple use of coroutineScope function`() {
        runBlocking {
            logger.info("caller job in runBlocking: ${coroutineContext.job}")
            try {
                val res = process(6)
                logger.info("res=$res")
            } catch (e: Exception) {
                logger.info("error: $e")
            }
        }
        
    }
    
    
    @Test
    fun `error on coroutine scope`() {
        runBlocking {
            logger.info("caller job in runBlocking: ${coroutineContext.job}")
            try {
                val res = processWithError(6)
                logger.info("res=$res")
            } catch (e: Exception) {
                logger.info("error: $e")
            }
        }
        
    }
    
    @Test
    fun `cancel the caller of coroutine scope`() {
        runBlocking {
            
            val job = launch {
                logger.info("caller job in runBlocking: ${coroutineContext.job}")
                try {
                    val res = process(6)
                    logger.info("res=$res")
                } catch (e: Exception) {
                    logger.info("error: $e")
                }
            }
            
            
            launch {
                delay(500)
                job.cancel()
            }
            
            job.join()
            logger.info("after job completed")
        }
        
    }
}