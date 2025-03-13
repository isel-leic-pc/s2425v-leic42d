package pt.isel.pc.monitors

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class CountDownLatch(private var counter : Int) {
    private val mutex = ReentrantLock()
    private val zeroReached = mutex.newCondition()

    init {
        require(counter > 0)
    }

    fun countDown()  {
        mutex.withLock {
            if (counter > 0) {
                if (--counter == 0) {
                    zeroReached.signalAll()
                }
            }
        }
    }

    fun await() {
        // fast path
        if (counter == 0) return
        // wait path
        do {
            zeroReached.await()
            if (counter == 0) return
        }
        while(true)
    }
}