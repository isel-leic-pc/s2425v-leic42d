package pt.isel.pc.monitors2

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * A unary acquire and release operations semaphore,
 * with support to cancellation by timeout and correct
 * InterruptedException processing
 */
class UnarySemaphore2(private var permits: Int = 0) {
    private val mutex = ReentrantLock()
    private val hasPermits = mutex.newCondition()

    init {
        require(permits >= 0)
    }

    private fun tryAcquire() : Boolean {
        if (permits > 0) {
            --permits
            return true
        }
        return false
    }

    @Throws(InterruptedException::class)
    fun acquire(timeout : Duration = Duration.INFINITE) : Boolean {
        mutex.withLock {
            // fast path
            if (tryAcquire()) return true
            if (timeout == Duration.ZERO)  return false
            // wait path
            var remaining = timeout.inWholeNanoseconds
            do {
                try {
                    remaining = hasPermits.awaitNanos(remaining)
                    if (tryAcquire()) return true
                    if (remaining < 0) return false
                }
                catch(e: InterruptedException) {

                    if (permits > 0) {
                        hasPermits.signal()
                    }
                    throw e

                    /**
                    the code below will alternatively
                    complete the acquire that has been interrupted,
                    delaying the interrupted exception in order to maintain
                    correct InterruptedException processing

                    if (permits > 0) {
                        Thread.currentThread().interrupt()
                        permits--
                        return true
                    }
                    throw e
                     */
                }
            }
            while(true)
        }
    }

    fun release()  {
        mutex.withLock {
            permits++
            hasPermits.signal()
        }
    }
}