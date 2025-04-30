package pt.isel.pc.coroutines1

import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.*
import kotlin.time.Duration

class ThreadPoolExecutor(
    private val maxThreadPoolSize: Int,
    private val keepAliveTime: Duration,
)  {
    
    @Throws(RejectedExecutionException::class)
    fun execute(continuation: Continuation<Unit>): Unit {
        TODO()
    }

    fun shutdown() : Unit {
      TODO()
    }

    @Throws(InterruptedException::class)
    fun awaitTermination(timeout: Duration): Boolean {
      TODO()
    }
    
    
}