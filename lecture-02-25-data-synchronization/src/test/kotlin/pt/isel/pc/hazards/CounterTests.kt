package pt.isel.pc.hazards

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

class CounterTests {
    @Test
    fun `increment counter by multile threads test`() {
        val counter = Counter()

        val NTHREADS = 20
        val NITERS = 1000000

        val threads : MutableList<Thread> =
            mutableListOf()

        repeat(NTHREADS) {
            val t = Thread {
                repeat(NITERS) {
                    counter.inc()
                }
            }
            threads.add(t)
        }

        threads.forEach {
            it.start()
        }
        threads.forEach {
            it.join()
        }

        assertEquals((NTHREADS*NITERS),counter.get() )
    }
}