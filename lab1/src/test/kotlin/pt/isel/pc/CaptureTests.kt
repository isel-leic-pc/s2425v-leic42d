package pt.isel.pc

import org.junit.jupiter.api.Test

class CaptureTests {
    @Test
    fun captureOfRepeatIndex() {
        val threads: MutableList<Thread> = ArrayList()

        repeat (100) {
            val t = Thread {
                println(it)
            }
            threads.add(t)
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }
    }

    @Test
    fun captureInACycle() {
        val threads: MutableList<Thread> = ArrayList()
        var x=0;
        while(x < 100) {
            val t = Thread {
                println(x)
            }
            threads.add(t)
            x++
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }
    }
}