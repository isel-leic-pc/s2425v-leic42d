package pt.isel.pc.mm

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class LazyTests {
    val NTHREADS = 100
    val VALUE = 2L

    class Value<T>(val value : T) {
        companion object {
            val instancesCount = AtomicInteger()
        }
        init {
            instancesCount.incrementAndGet()
        }
    }

    private fun lazyTest(lazy: LazyBuilder<Value<Long>> ) {
        val values = LongArray(NTHREADS)
        val threads = (0 until NTHREADS).map { index->
            Thread {
                values[index] = lazy.get().value
            }
        }
        for ( t in threads) {
            t.start()
        }
        for ( t in threads) {
            t.join(2000)
            assertFalse(t.isAlive)
        }
        for(v in values) {
            assertEquals(VALUE, v)
        }
        assertEquals(1, Value.instancesCount.get())
    }

    @Test
    fun `Lazy0 method correction test`() {
        val lazy = Lazy0 { Value(VALUE) }
        lazyTest(lazy)

    }

    @Test
    fun `Lazy1 method correction test`() {
        val lazy = Lazy1 { Value(VALUE) }
        lazyTest(lazy)
    }

    @Test
    fun `Lazy2 method correction test`() {
        val lazy = Lazy2 { Value(VALUE) }
        lazyTest(lazy)
    }

    @Test
    fun `kotlin  method correction test`() {
        val NTHREADS = 100
        val VALUE = 2L

        val  lazy : Value<Long>  by lazy {
            Value(VALUE)
        }
        val values = LongArray(NTHREADS)
        val threads = (0 until NTHREADS).map { index->
            Thread {
                values[index] = lazy.value
            }
        }
        for ( t in threads) {
            t.start()
        }
        for ( t in threads) {
            t.join(2000)
            assertFalse(t.isAlive)
        }
        for(v in values) {
            assertEquals(VALUE, v)
        }
        assertEquals(1, Value.instancesCount.get())
    }
}