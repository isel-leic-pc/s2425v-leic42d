package pt.isel.pc.coroutinex

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
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
        catch (e: Exception) {
            logger.info("catched in doCalc: $e")
            throw e
        }
    }
    
   
    private suspend fun doEffect() {
        try {
            delay(2000)
            logger.info("effect done")
        }
        catch (e: Exception) {
            logger.info("cached in doEffect: $e")
            throw e
        }
      
    }
    
    private suspend fun process(value: Int) : Int = coroutineScope {
        val parentJob = coroutineContext.job
        logger.info("parent job in coroutineScope: $parentJob")
        logger.info("parent of parentJob in coroutine scope: ${parentJob.parent}")
        logger.info("context for coroutineScope:")
        showContext()
        
        launch {
            doEffect()
        }
        val resultDefer = async {
            logger.info("context for coroutineScope inner coroutine:")
            showContext()
            doCalc(value)
        }
        resultDefer.await()
     
    }
    
   
    
    @Test
    fun `simple use of coroutineScope function`() {
        runBlocking {
            logger.info("caller job in runBlocking: ${coroutineContext.job}")
            val res = process(6)
            logger.info("res=$res")
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
    
    
    private suspend fun processWithError(value: Int) : Int = coroutineScope {
        val parentJob = coroutineContext.job
        logger.info("parent job in coroutineScope: $parentJob")
        logger.info("parent of parentJob in coroutine scope: ${parentJob.parent}")
        logger.info("context for coroutineScope:")
        showContext()
        
        val resultDefer = async {
            doCalcWithError(value)
        }
        
        launch {
            delay(3000)
            logger.info("launch termination")
        }
        resultDefer.await()
        
    }
    
    @Test
    fun `error on coroutine scope`() {
        runBlocking {
            logger.info("caller job in runBlocking: ${coroutineContext.job}")
            try {
                val res = processWithError(6)
                logger.info("res=$res")
            } catch (e: Throwable) {
                logger.info("error on processWithError: $e")
            }
            logger.info("end of main coroutine in runBlocking")
        }
        logger.info("test done")
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
    
    private suspend fun processWithContext(value: Int) : Int =
        withTimeout(1000L) {
            withContext(Dispatchers.Default) {
                val parentJob = coroutineContext.job
                logger.info("parent job in processWithContext: $parentJob")
                logger.info("parent of parentJob in processWithContext: ${parentJob.parent}")
                logger.info("context for processWithContext:")
                showContext()
                
                launch {
                    doEffect()
                }
                val resultDefer = async {
                    logger.info("context for processWithContext inner coroutine:")
                    showContext()
                    doCalc(value)
                }
                resultDefer.await()
            }
        }
    
    @Test
    public fun coroutineScopeWithContext() {
        runBlocking {
            logger.info("caller job in runBlocking: ${coroutineContext.job}")
            try {
                val res = processWithContext(6)
                logger.info("res=$res")
            }
            catch(e: Exception) {
                logger.info("catched error in runBloking: $e")
            }
         
        }
    }
}