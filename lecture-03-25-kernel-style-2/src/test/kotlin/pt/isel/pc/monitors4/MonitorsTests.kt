package pt.isel.pc.monitors4

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import java.util.concurrent.atomic.AtomicInteger

class MonitorsTests {

    private fun joinThreadsWithTimeout(threads: List<Thread>, timeout: Long = 1000) : Boolean {
        var dueTime = System.currentTimeMillis() + timeout
        var remaining = timeout
        for(thread in threads) {
            thread.join(remaining)
            remaining = dueTime - System.currentTimeMillis()
            if (thread.isAlive() || remaining <= 0) {
                return false
            }
        }
        return true
    }


    @Test
    fun `classic monitor style for broadcast event breaks on a sudden reset`(){
        val NTHREADS = 1000
        repeat(100) {
            val event = EventBroadcast()
            var count = AtomicInteger()
            val threads = (1..NTHREADS).map {
                Thread {
                    event.await()
                    count.incrementAndGet()
                    //println("Thread ${Thread.currentThread().name} finished waiting")
                }
            }
            threads.forEach { it.start() }
            Thread.sleep(100)
            event.set()
           // Thread.sleep(5)
            event.reset()
            joinThreadsWithTimeout(threads, 1000)
            println(it)
            assertEquals(NTHREADS, count.get())
        }
    }
}