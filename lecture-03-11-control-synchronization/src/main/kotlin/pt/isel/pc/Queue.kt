package pt.isel.pc

import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * an unlimited FIFO queue, blocking on get when empty
 */
class Queue<T>(private val capacity: Int) {
    private val list = LinkedList<T>()
    private val mutex = ReentrantLock()
    private val availableItems = Semaphore(0)
    private val availableSpace = Semaphore(capacity)

    fun offer(elem : T)  {
        availableSpace.acquire()
        mutex.lock()
        try {
            list.add(elem)
        }
        finally {
            mutex.unlock()
        }
        availableItems.release()
    }
    fun take() : T? {
        availableItems.acquire()
        mutex.lock()
        try {
            return list.removeFirst()
        }
        finally {
            mutex.unlock()
        }
        availableSpace.release()
    }
}