package pt.isel.pc.monitors4

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * Here we use a simpler approach to the Batch object use in EventBroadcastBatch
 * alternative. This is based on the concept of notification generation, and use
 * a simple integer to define that generation.
 * Note that this is only possible when the notification
 * is not associated to data that must be send to the waiters.
 * If this is the case, a Batch object must be used to include
 * the necessary data send to batch waiters
 */
class EventBroadcastGen(private var signaled : Boolean = false) {
    private val monitor = ReentrantLock()
    private val signalEvent = monitor.newCondition()

    private var generation = 0

    fun await(timeout : Duration = Duration.INFINITE) : Boolean {
        monitor.withLock {
            // fast path
            if (signaled) return true
            if (timeout == Duration.ZERO) return false
            // wait path
            var remaining = timeout.inWholeNanoseconds
            val myGen = generation

            do {
                try {
                    remaining = signalEvent.awaitNanos(remaining)
                    // here we just check our "waiter" object
                    // if "done" is true than we just know that
                    // operation is completed, regardless the current
                    // "signaled" state
                    if (myGen != generation) return true
                    if (remaining <= 0) {
                        return false
                    }
                }
                catch(e: InterruptedException) {
                    if (myGen != generation) {
                        Thread.currentThread().interrupt()
                        return true
                    }
                    throw e
                }
            }
            while(true)
        }
    }

    fun set() {
        monitor.withLock {
            signaled = true

            // the "set" operation is responsible
            // to inform all waiters that those operations are concluded
            // in this version this sis done just by increase the generation

            generation++
            signalEvent.signalAll()
        }
    }

    fun reset() {
        monitor.withLock {
            signaled = false
        }
    }
}