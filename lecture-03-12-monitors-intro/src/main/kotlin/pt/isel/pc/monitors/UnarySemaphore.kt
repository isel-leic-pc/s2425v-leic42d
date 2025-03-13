package pt.isel.pc.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class UnarySemaphore(private var permits: Int = 0) {
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

    fun acquire() {
        mutex.withLock {
            // fast path
            if (tryAcquire()) return
            // wait path
            do {
                hasPermits.await()
                if (tryAcquire()) return
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