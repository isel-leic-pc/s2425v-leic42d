package pt.isel.pc.threadsintro

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


import java.lang.Thread.sleep

class ThreadTests {
    val logger = KotlinLogging.logger {}

    /**
     * code for threads created in tests
     */
    fun threadCode() {
        sleep(2000)
        logger.info("new thread is in state ${Thread.currentThread().state}")
    }

    @Test
    fun `thread object creation test`() {
        logger.info("test started on thread ${Thread.currentThread().name}")


        val thread = Thread()
        // the thread is a mere object, no code is associated to it
        logger.info("new thread name is ${thread.name} and state ${thread.state}")
        logger.info("test terminated on thread ${Thread.currentThread().name}")
    }

    @Test
    fun `thread creation and execution test`() {
        logger.info("test started on thread ${Thread.currentThread().name}")
        // the associated code is passed to constructor
        val thread = Thread(::threadCode)
        logger.info("call new thread start")
        // start method creates the associated platform (SO) thread
        // (which we call the virtual processor) to run the code
        thread.start()
        logger.info("new thread name is ${thread.name} and state ${thread.state}")

        // no output from the thread occurs. Why?
        logger.info("test terminated on thread ${Thread.currentThread().name}")
    }

    @Test
    fun `create thread and wait for her termination test`() {
        logger.info("test started on thread ${Thread.currentThread().name}")
        val newThread = Thread(::threadCode)
        logger.info("call new thread start")
        newThread.start()

        // the join synchronizes the test thread with the termination
        // of the new thread, that is the test thread is blocked until
        // the termination of new thread. So the output produced by the new thread
        // appears now, guaranteed
        newThread.join()

        logger.info("new thread inn in state ${newThread.state}")
        logger.info("test terminated on thread ${Thread.currentThread().name}")
    }

    @Test
    fun `create multiple threads`() {
        logger.info("test started on thread ${Thread.currentThread().name}")
        val threads = listOf(
            Thread(::threadCode),
            Thread(::threadCode),
            Thread(::threadCode))

        threads.forEach {
            it.start()
        }
        threads.forEach {
            it.join()
        }
        threads.forEach {
           assertEquals(Thread.State.TERMINATED, it.state)
        }


        logger.info("test terminated on thread ${Thread.currentThread().name}")
    }

    @Test
    fun `create thread define the thread code with a lambda`() {
        logger.info("test started on thread ${Thread.currentThread().name}")

        val threads = listOf(1, 2,3)
            .map {
                index ->
                Thread {
                    Thread.sleep(2000)
                    logger.info("new thread is in state ${Thread.currentThread().state}")
                }
            }

        threads.forEach {
            it.start()
        }
        threads.forEach {
            it.join()
        }
        threads.forEach {
            assertEquals(Thread.State.TERMINATED, it.state)
        }

        logger.info("test terminated on thread ${Thread.currentThread().name}")
    }

    class MyThread : Thread() {
        val logger = KotlinLogging.logger {}
        override fun run() {
            Thread.sleep(2000)
            logger.info("new thread instance of MyThread with name ${Thread.currentThread().name} and state ${Thread.currentThread().state} ")
        }
    }

    @Test
    fun `create thread using a Thread subclass`() {
        logger.info("test started on thread ${Thread.currentThread().name}")
        val threads = listOf(1, 2,3)
            .map {
                index ->
                MyThread()
            }

        threads.forEach {
            it.start()
        }
        threads.forEach {
            it.join()
        }
        threads.forEach {
            assertEquals(Thread.State.TERMINATED, it.state)
        }
        logger.info("test terminated on thread ${Thread.currentThread().name}")
    }
}

