package pt.isel.pc.mm

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

interface LazyBuilder<T> {
    fun get(): T
}
/**
 * obviously incorrect implementation
 * has atomic, visibility and reordering problems
 */
class Lazy0<T>(private val factory : () -> T) : LazyBuilder<T> {

    var value : T? = null

    override fun get() : T {
        if (value == null) {
            value = factory()
        }
        return value!!
    }
}

/**
 * A correct, but very inefficient implementation.
 */
class Lazy1<T>(private val factory : () -> T): LazyBuilder<T> {
    private val lock = ReentrantLock()
    private var value : T? = null

    override fun get() : T {
        lock.withLock {
            if (value == null) {
                value = factory()
            }
        }
        return value!!
    }
}

/**
 * A correct and efficient implementation
 * using double check locking (DCL) pattern
 */
class Lazy2<T>(private val factory : () -> T) : LazyBuilder<T> {
    val lock = ReentrantLock()

    @Volatile
    private var value : T? = null

    override fun get() : T {
        if (value == null) {
            lock.withLock {
                if (value == null) {
                    value = factory()
                }

            }
        }
        return value!!
    }
}



