package pt.isel.pc.monitors4

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

class EventBroadcastGen(private var signaled : Boolean = false) {
    // thw waiter node
    class Batch {
        internal var done = false
    }

    private val monitor = ReentrantLock()
    private val signalEvent = monitor.newCondition()

    // the waiter batch
    private var current = Batch()
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