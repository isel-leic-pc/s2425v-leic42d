package pt.isel.pc.monitors4

import java.util.LinkedList
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * A fair alternative to previous "NArySemaphore"
 * Here we use "kernel style" to guarantee FIFO order
 * on "acquire" requests, regardless their amount
 * We also use SPECIFIC NOTIFICATION, that is, a different condition is associated
 * to each pending acquire, in order to avoid broadcast signalization.
 */
class NArySemaphoreFairSN(private var permits : Int = 0) {
    private val mutex = ReentrantLock()
    private val hasPermits = mutex.newCondition()

    /**
     *  A Waiter needs to inform the  needed permits (units),
     *  a flag to be informed for the acquiring completion,
     *  and a unique condition for specific signalization of this request
     */
    private class Waiter(val units : Int, val condition: Condition) {
        var done = false
    }

    // the pending acquires list
    private val waiters = LinkedList<Waiter>()

    init {
        require(permits >= 0)
    }

    private fun tryAcquire(units: Int ) : Boolean {
        if (permits >= units) {
            permits -= units
            return true
        }
        return false
    }

    /**
     * When the semaphore state changes (on a release or cancellation due to
     * timeout or external interruption) this auxiliary method awakes
     * all possible pending acquires
     */
    private fun tryResolvePendingAcquires() {
        while(waiters.size > 0 && waiters.first.units <= permits) {
            val waiter = waiters.removeFirst()
            waiter.done = true
            permits -= waiter.units
            waiter.condition.signal()
        }

    }

    @Throws(InterruptedException::class)
    fun acquire(units : Int, timeout : Duration = Duration.INFINITE) : Boolean {
        mutex.withLock {
            // fast path
            if (tryAcquire(units)) return true
            if (timeout == Duration.ZERO) return false
            // wait path
            var remaining = timeout.inWholeNanoseconds
            val waiter = Waiter(units, mutex.newCondition())
            waiters.add(waiter)

            do {
                try {
                    remaining = hasPermits.awaitNanos(remaining)
                    if (waiter.done) return true
                    if (remaining < 0) {
                        waiters.remove(waiter)
                        tryResolvePendingAcquires()
                        return false
                    }
                }
                catch(e: InterruptedException) {
                    if (waiter.done) {
                        Thread.currentThread().interrupt()
                        return true
                    }
                    waiters.remove(waiter)
                    tryResolvePendingAcquires()
                    throw e
                }
            } while (true)
        }
    }

    fun release(units: Int)  {
        mutex.withLock {
            permits += units
            tryResolvePendingAcquires()
        }
    }
}