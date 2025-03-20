package pt.isel.pc.monitors2

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * An implementation of an n-ary acquires and releases using a
 * monitor with classic monitor style.
 * Since monitor style is subject to barging, this implementation is unfair,
 * because small size acquires have a bigger chance to complete, delaying
 * the completion of bigger size acquires.
 */
class NArySemaphore(private var permits : Int = 0) {
    private val mutex = ReentrantLock()
    private val hasPermits = mutex.newCondition()

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
     * Note that in this case we don't need to explicitly catch
     * InterruptedException because the broadcast condition sinalization
     * done in release operation
     */
    @Throws(InterruptedException::class)
    fun acquire(units : Int, timeout : Duration = Duration.INFINITE) : Boolean {
        mutex.withLock {
            // fast path
            if (tryAcquire(units)) return true

            if (timeout == Duration.ZERO) return false   // the try fail
            // wait path
            var remaining = timeout.inWholeNanoseconds
            do {
                remaining = hasPermits.awaitNanos(remaining)
                if (tryAcquire(units)) return true
                if (remaining < 0) return false
            } while (true)
        }
    }

    fun release(units: Int)  {
        mutex.withLock {
            permits += units
            // we must do a broadcast signalization
            // since we don't what acquires can be completed by this release
            hasPermits.signalAll()
        }
    }
}