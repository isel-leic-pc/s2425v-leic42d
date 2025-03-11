package pt.isel.pc.lab2


import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * a FIFO queue with blocking put when full,
 * and blocking get when empty
 */
class Queue<T>(private val capacity : Int) {
    private val list = LinkedList<T>()
    private val mutex = ReentrantLock()
    private val itemsAvailable = Semaphore(0)
    private val spaceAvailable = Semaphore(capacity)

    fun offer(elem : T)  {
        spaceAvailable.acquire()
        mutex.withLock {
            list.add(elem)
        }
        itemsAvailable.release()
    }

    fun take() : T {
        itemsAvailable.acquire()
        return mutex.withLock {
            list.removeFirst()
        }.also {
            spaceAvailable.release()
        }
    }
}