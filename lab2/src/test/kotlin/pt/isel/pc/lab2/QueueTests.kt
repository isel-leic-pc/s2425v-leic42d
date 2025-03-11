package pt.isel.pc.lab2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class QueueTests {

    @Test
    fun `a single consumer and producer trivial test`() {
        val queue = Queue<Int>(1)
        var result = 0

        val tConsumer = Thread {
            result = queue.take()
        }
        tConsumer.start()

        val tProducer = Thread {
            Thread.sleep(1500)
            queue.offer(2)
        }
        tProducer.start()

        // Join with timeout is useful,
        // in order to guarantee the test terminate in bounded time
        // Note the total timeout here may exceed 3000 ms
        // This must be better handled when the number of test threads increase
        tProducer.join(3000)
        tConsumer.join(3000)

        assertEquals(2, result)
    }

    @Test
    fun `multiple use with a single consumer and producer test`() {
        val queue = Queue<Int>(10)
        val consumedValues = mutableSetOf<Int>()
        val NVALUES = 100_000

        val tconsumer = Thread {
            while(true) {
                val res = queue.take()
                if (res < 0) break
                consumedValues.add(res)
            }
        }
        tconsumer.start()

        val tproducer = Thread {
            repeat(NVALUES) {
                queue.offer(it)
            }
            queue.offer(-1)
        }
        tproducer.start()

        tproducer.join(3000)
        tconsumer.join(3000)

        assertEquals(NVALUES, consumedValues.size)
    }

}