package pt.isel.pc.mm

import mu.KotlinLogging
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock


private val logger = KotlinLogging.logger {}


/**
 * note: an incorrect implementation
 * The following variants offer different correct solutions
 */
class Done0 {
    private var ready : Boolean = false

    fun set() {
        ready = true
    }

    fun get() : Boolean {
        return ready
    }
}

class Done1 {
    private var ready : Boolean = false
    private val lock = ReentrantLock()

    fun set() {
        lock.withLock {
            ready = true
        }

    }

    fun get() : Boolean {
        lock.withLock {
            return ready
        }

    }
}

class Done2 {
    val ready = AtomicBoolean(false)

    fun set() {
        ready.set(true)
    }

    fun get() : Boolean = ready.get()
}

class Done3 {

    @Volatile
    var ready = false

    fun set() {
        ready = true
    }

    fun get() : Boolean = ready
}

private var result = 0
val done = Done0()

fun main() {
    logger.info("Start!")

    val t = Thread {
        while(!done.get());
        logger.info("number: $result")
    }
    done.set()
    t.start()
    sleep(50)
    result = 42


    t.join()
    logger.info("Done!")
}