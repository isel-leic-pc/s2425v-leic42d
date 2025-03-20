package pt.isel.pc.monitors3

import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.sign
import kotlin.time.Duration

/**
 * This alternative (for EventBroadcast0) solves the problem found there
 * using a "kernel style" approach, that guarantees that all  threads awaked
 * by "set" operation will not block again due to barging
 */
class EventBroadcast(private var signaled : Boolean = false) {
    // thw waiter node
    class Waiter {
        internal var done = false
    }

    private val monitor = ReentrantLock()
    private val signalEvent = monitor.newCondition()

    // the waiter list
    private val waiters = LinkedList<Waiter>()


    fun await(timeout : Duration = Duration.INFINITE) : Boolean {
        monitor.withLock {
            // fast path
            if (signaled) return true
            if (timeout == Duration.ZERO) return false
            // wait path
            var remaining = timeout.inWholeNanoseconds
            val waiter = Waiter()
            waiters.add(waiter)
            do {
                try {
                    remaining = signalEvent.awaitNanos(remaining)
                    // here we just check our "waiter" object
                    // if "done" is true than we just know that
                    // operation is completed, regardless the current
                    // "signaled" state
                    if (waiter.done) return true
                    if (remaining < 0) {
                        waiters.remove(waiter)
                        return false
                    }
                }
                catch(e: InterruptedException) {
                    if (waiter.done) {
                        Thread.currentThread().interrupt()
                        return true
                    }
                    waiters.remove(waiter)
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
            // just setting the done flag and clearing the waiting list
            for(w in waiters) {
                w.done = true
            }
            waiters.clear()
            signalEvent.signalAll()
        }
    }

    fun reset() {
        monitor.withLock {
            signaled = false
        }
    }
}