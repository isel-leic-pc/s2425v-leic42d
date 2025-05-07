package pt.isel.pc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.system.measureTimeMillis

class MutexVersusReentrantLockTests {

    private val logger = KotlinLogging.logger {}
    @Test
    fun `massive coroutines mutex use versus reentrant lock`() {
        val NCOROUTINES = 2
        val NITERS = 500000
        var count = 0

        var timeMillis = measureTimeMillis {
            runBlocking {
                val mutex = Mutex()
                repeat (NCOROUTINES) {
                    launch() {
                        repeat(NITERS) {
                            mutex.withLock {
                                count++
                            }
                        }
                    }
                }
            }
        }
        assertEquals(NCOROUTINES*NITERS, count)

        println("with mutex done in $timeMillis ms!")

        count = 0

        timeMillis = measureTimeMillis {
            runBlocking() {

                val mutex = ReentrantLock()
                repeat (NCOROUTINES) {
                    launch() {
                        repeat(NITERS) {
                            mutex.withLock {
                                count++
                            }
                        }
                    }
                }
            }
        }
        assertEquals(NCOROUTINES*NITERS, count)

        println("with reentrantLock done in $timeMillis ms!")

    }

    
    @Test
    fun `use reentrant lock between suspend function calls`() {
        val mutex = ReentrantLock()
        var reentrancyLockCount = 0
        runBlocking() {
            val job1 = launch {
                try {
                    mutex.lock()
                    reentrancyLockCount++
                    logger.info("cr1 before delay")
                    delay(3000)
                    logger.info("cr1 after delay")
                    reentrancyLockCount--
                }
                finally {
                    mutex.unlock()
                }
            }
            val job2 = launch {
                delay(500)
                try {
                    mutex.lock()
                    reentrancyLockCount++
                    logger.info("cr1 before delay")
                    assertTrue(reentrancyLockCount<=1)

                    delay(3000)
                    logger.info("cr1 after delay")
                    reentrancyLockCount--

                }
                finally {
                    mutex.unlock()
                }
            }

        }
        assertTrue(!mutex.isLocked)

    }
    
    @Test
    fun `use mutex between suspend function calls`() {
        val mutex = Mutex()
        var reentrancyLockCount = 0
        runBlocking {
            val job1 = launch {
                try {
                    mutex.lock()
                    reentrancyLockCount++
                    logger.info("cr1 before delay")
                    delay(3000)
                    logger.info("cr1 after delay")
                    reentrancyLockCount--
                }
                finally {
                    mutex.unlock()
                }
            }
            val job2 = launch {
                delay(500)
                try {
                    mutex.lock()
                    reentrancyLockCount++
                    logger.info("cr1 before delay")
                    assertTrue(reentrancyLockCount==1)
                    
                    delay(3000)
                    logger.info("cr1 after delay")
                    reentrancyLockCount--
                    
                }
                finally {
                    mutex.unlock()
                }
            }
            
        }
        assertTrue(!mutex.isLocked)
        
    }
}