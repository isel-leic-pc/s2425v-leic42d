package pt.isel.pc.flows

import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import kotlin.test.Test

class HotFlowTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    
    
    class BoundedBroadcastFlow<T>(val capacity : Int) {
        private val sharedFlow = MutableSharedFlow<T>(capacity)

        suspend fun write(t: T) {
            sharedFlow.emit(t)
        }

        fun read() : Flow<T> =
            sharedFlow.asSharedFlow()
    }
    
    
    
    
    @Test
    fun `bounded broadcast flow test`() {
        val bcFlow = BoundedBroadcastFlow<Int>(2)
        
        runBlocking(Dispatchers.IO) {
            val sender = launch {
                repeat(10) {
                    bcFlow.write(it)
                    delay(100)
                }
            }
            
            delay(500)
            val reader1 = launch {
                bcFlow.read().collect {
                    logger.info("reader1 collect $it")
                }
            }
            
            val reader2 = launch {
                bcFlow.read().collect {
                    logger.info("reader2 collect $it")
                }
            }
        }
    }
    
}