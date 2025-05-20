package pt.isel.pc.coroutinesx

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
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
            } catch (e: Exception) {
                logger.info("error: $e")
            }
        }
    }
}