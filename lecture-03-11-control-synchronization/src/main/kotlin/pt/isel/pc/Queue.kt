package pt.isel.pc

import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * an unlimited FIFO queue, blocking on get when empty
 */
class Queue<T>() {
    private val list = LinkedList<T>()

    fun put(elem : T)  {
        TODO()
    }

    fun get() : T {
        TODO()
    }
}