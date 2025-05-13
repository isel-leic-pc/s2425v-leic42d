package pt.isel.pc.coroutinesx

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import pt.isel.pc.nio.coroutinesx.showContext
import kotlin.test.Test

class CoroutineScopeFunctionTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    
    private suspend fun doCalc(value: Int) : Int {
        TODO()
    }
    
    private suspend fun process(value: Int) : Int {
        TODO()
    }
    
    @Test
    fun `error on coroutine scope`() {
        runBlocking {
            try {
                val res = process(6)
                logger.info("res=$res")
            }
            catch(e: Exception) {
                logger.info("error: $e")
            }
        }
    }
}