package pt.isel.pc.coroutinesx.flows

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class FlowsTests {
    
    @Test
    fun `first ten evens`() {
        val evens = flow2 {
            repeat(10) {
                emit((it +1)*2)
                delay(1000)
            }
        }
        runBlocking {
            evens
            .limit(4)
            .map {
                it + 1
            }
            .collect {
                println(it)
            }
        }
       
    }
}