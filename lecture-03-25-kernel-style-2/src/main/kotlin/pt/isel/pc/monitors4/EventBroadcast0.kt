package pt.isel.pc.monitors4

import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * This is a "wrong" solution for a synchronizer that
 * produce broadcast events when signaled, that is,
 * awakes (on "set" operation") all threads waiting for "signaled" state
 * (on "await") operation
 * After set all threads found the synchronizer in "signaled" state and not block
 * until a "reset" operation is done, that put the synchronizer in "non signaled" state
 *
 * The solution is wrong since, due to barging, a awaked thread can find
 * the synchronizer in "non signaled" state, blocking again, if a "reset"
 * operation is done before the awaked thread can reenter the monitor.
 *
 * This is solved using "kernel style" monitor construction, as found on
 * alternative solution "EventBroadcast"
 */
class EventBroadcast0(private var signaled : Boolean = false) {

    private val monitor = ReentrantLock()
    private val signalEvent = monitor.newCondition()

    /**
     * "awaits" for synchronizer "signaled" state
     */
    fun await(timeout : Duration = Duration.INFINITE) : Boolean {
        monitor.withLock {
            // fast path
            if (signaled) return true
            if (timeout == Duration.ZERO) return false
            // wait path
            var remaining = timeout.inWholeNanoseconds
            do {
                remaining = signalEvent.awaitNanos(remaining)
                if (signaled) return true
                if (remaining <= 0) return false
            }
            while(true)
        }
    }

    /**
     * "set" operation just puts the synchronizer in "signaled" state
     * and notify all waiters
     */
    fun set() {
        monitor.withLock {
            signaled = true
            signalEvent.signalAll()
        }
    }

    /**
     * reset just put the synchronizer in "non signaled" state
     */
    fun reset() {
        monitor.withLock {
            signaled = false
        }
    }
}