package pt.isel.pc

import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch

class ThreadInterruptionTests {
    private val logger = KotlinLogging.logger {}

    @Test
    fun `not attending interrupt test`() {
        var result = 0L

        val thread = Thread {

            logger.info("start thread")
            var res = 1L
            repeat(2_000_000_000) {
                if (Thread.currentThread().isInterrupted) {
                    throw InterruptedException()
                }
                res += it+1
            }
            result = res
            logger.info("end thread")
        }
        thread.start()
        sleep(100)
        thread.interrupt()
        logger.info("thread interrupted")
        thread.join()
        println("result= $result")

    }

    @Test
    fun `attending interrupt test`() {

        val thread = Thread {
            logger.info("start thread")
            sleep(1000)
            logger.info("end thread")
        }
        thread.start()
        sleep(500)
        thread.interrupt()
        logger.info("thread interrupted")
        thread.join()

    }
}