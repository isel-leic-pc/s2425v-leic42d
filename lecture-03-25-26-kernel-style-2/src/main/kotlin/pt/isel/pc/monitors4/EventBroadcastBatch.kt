package pt.isel.pc.monitors4

import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

class EventBroadcastBatch(private var signaled : Boolean = false) {
    // the waiter node
    class Batch {
        internal var done = false
    }

    private val monitor = ReentrantLock()
    private val signalEvent = monitor.newCondition()

    // the waiter batch
    private var current = Batch()


    fun await(timeout : Duration = Duration.INFINITE) : Boolean {
        monitor.withLock {
            // fast path
            if (signaled) return true
            if (timeout == Duration.ZERO) return false
            // wait path
            var remaining = timeout.inWholeNanoseconds
            val myBatch = current

            do {
                try {
                    remaining = signalEvent.awaitNanos(remaining)
                    // here we just check the observed Batch object
                    // if "done" is true than we just know that
                    // operation is completed, regardless the current
                    // "signaled" state
                    if (myBatch.done) return true
                    if (remaining <= 0) {
                        return false
                    }
                }
                catch(e: InterruptedException) {
                    if (myBatch.done) {
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
            // to inform all waiters that their operations are concluded.
            // In this version, just setting the current batch done flag and
            // and creating a new batch for the next round
            current.done = true
            current = Batch()
            signalEvent.signalAll()
        }
    }

    fun reset() {
        monitor.withLock {
            signaled = false
        }
    }
}