package pt.isel.pc.monitors4

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.min
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

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

    @Test
    fun testFairSemaphore() {
        val sem = NArySemaphoreFairSN()
        val t = Thread {
            sem.acquire(1)
        }
        t.start()
        val start = System.currentTimeMillis()
        t.join(2000)
        assertTrue(System.currentTimeMillis() >= start + 2000)
    }


    @Test
    fun `lots of insane releases and acquires test`() {
        val sem = NArySemaphoreFairSN()

        val RELEASERS = 20
        val ACQUIRERS = 10
        val RELEASE_ACQUIRE_LIMIT = 1000000
        var totalReleases = 0
        var totalAcquires = 0
        val releaserMutex = ReentrantLock()
        val acquiresMutex = ReentrantLock()
        val releasers = (1..RELEASERS).map {
            index->
            Thread {
                while(true) {
                    var units : Int = 0
                    releaserMutex.withLock {
                        if (totalReleases == RELEASE_ACQUIRE_LIMIT)  {
                            logger.info("releaser $index terminated")
                            return@Thread
                        }
                        units = Random.nextInt(1, 4)
                        if ((units + totalReleases) > RELEASE_ACQUIRE_LIMIT) {
                            units = RELEASE_ACQUIRE_LIMIT - totalReleases
                        }
                        totalReleases += units
                    }
                    sem.release(units)
                }

            }
        }

        val acquirers = (1..ACQUIRERS).map {
            index->
            Thread {
                logger.info("acquirer $index started")
                while(true) {
                    var units : Int = 0
                    acquiresMutex.withLock {
                        if (totalAcquires == RELEASE_ACQUIRE_LIMIT) {
                            logger.info("acquirer $index terminated")
                            return@Thread
                        }
                        units = Random.nextInt(1, 10)
                        if ((units + totalAcquires) > RELEASE_ACQUIRE_LIMIT) {
                            units = RELEASE_ACQUIRE_LIMIT - totalAcquires
                        }
                        totalAcquires += units
                    }
                    sem.acquire(units)
                    //logger.info("acquirer $index acquires $units")
                }

            }
        }

        acquirers.forEach { it.start()}
        releasers.forEach { it.start()}

        assertTrue(joinThreadsWithTimeout(releasers, 5000))
        assertTrue(joinThreadsWithTimeout(acquirers, 5000))
    }
}