package pt.isel.pc.hazards

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Counter(private var value: Long = 0) {
    private val mutex = ReentrantLock()

    init {  // This is the constructor
        require(value >= 0)
    }

    fun inc() {
//        mutex.lock()
//        try {
//            value++
//        } finally {
//            mutex.unlock()
//        }
        mutex.withLock {
            value++
        }
    }


    fun dec() {
        if (value > 0) {
            value--
        }
    }

    fun get() : Long {
        return value
    }
}

class AtomicCounter(value: Long = 0) {
    private val value = AtomicLong(value)

    init {  // This is the constructor
        require(value >= 0)
    }

    fun inc() {
        value.incrementAndGet()
    }


    fun dec() {
        if (value.get() > 0) {
            value.decrementAndGet()
        }
    }

    fun get() : Long {
        return value.get()
    }
}


