package pt.isel.pc.futures

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import java.util.concurrent.Executors
import java.util.concurrent.Future



class FuturesTests {
    private val logger = KotlinLogging.logger {}
 

    @Test
    fun `launch future tasks and get result`() {
        val executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors())
 
        val f1 : Future<Int> = executor.submit<Int> {
            sleep(1000)
            logger.info("future task 1 done")
            3
        }
        val f2 = executor.submit<Int> {
            sleep(2000)
            logger.info("future task 2 done")
            5
        }

        val v1 = f1.get()
        val v2 = f2.get()

        assertEquals(3, v1)
        assertEquals(5, v2)
        logger.info("service done with $v1 and $v2")

    }
    
}