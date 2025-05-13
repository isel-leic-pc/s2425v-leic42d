package pt.isel.pc.nio.coroutinesx.dispatchers

import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.jvm.Throws
import kotlin.time.Duration

class ThreadPoolExecutor (
    private val maxThreadPoolSize: Int,
    private val keepAliveTime: Duration,
)   {
    enum class PoolState { ACTIVE, SHUTDOWN_STARTED, TERMINATED }

    private var poolState = PoolState.ACTIVE
    private var threadsNumber = 0

    private val mutex = ReentrantLock()
    private val terminationDone = mutex.newCondition()

    private val requestQueue = LinkedList<Continuation<Unit>>()
    private val waitingThreads = LinkedList<WorkerThread>()
    private val terminationAwaiters = LinkedList<Continuation<Unit>>()
    private var number = AtomicInteger()
    
    inner class WorkerThread(var code: Continuation<Unit>?) : Thread("TPS thread ${number.incrementAndGet()}") {
        val hasWork = mutex.newCondition()
        
        private fun safeExec() {
            try {
                code?.resume(Unit)
                code = null
            }
            catch(e: Exception) {
              
                // just swallow the exception
                // in order to avoid abnormal thread termination
            }
        }
        
        private fun terminate() {
            threadsNumber--
            if (threadsNumber == 0 && poolState == PoolState.SHUTDOWN_STARTED) {
                poolState = PoolState.TERMINATED
                terminationDone.signalAll()
                for(c in terminationAwaiters) {
                    c.resume(Unit)
                }
            }
        }

        override fun run( ) {
            do {
                safeExec()
                mutex.withLock {
                    // fast path
                    if (requestQueue.isNotEmpty()) {
                        code = requestQueue.removeFirst()
                    }
                    else {
                        var remaining = keepAliveTime.inWholeNanoseconds
                        // waiting path
                        waitingThreads.add(this)
                        while(true) {
                            if (remaining <= 0 || poolState != PoolState.ACTIVE && requestQueue.size > 0) {
                                terminate()
                                return
                            }
                            remaining = hasWork.awaitNanos(remaining)
                            if (code != null) break;
                        }
                    }
                }
            }
            while(true)

        }
    }
    
    @Throws(RejectedExecutionException::class)
    fun execute(continuation: Continuation<Unit>): Unit {
        mutex.withLock {
            if (poolState != PoolState.ACTIVE) throw RejectedExecutionException()
            if (waitingThreads.isNotEmpty()) {
                val t = waitingThreads.removeFirst()
                t.code = continuation
                t.hasWork.signal()
            }
            else if (threadsNumber < maxThreadPoolSize) {
                threadsNumber++
                WorkerThread(continuation).start()
            }
            else {
                requestQueue.add(continuation)
            }
        }
    }

    fun shutdown() : Unit {
        mutex.withLock {
            if (poolState != PoolState.ACTIVE) return
            if (threadsNumber == 0) {
                poolState = PoolState.TERMINATED
                terminationDone.signalAll()
            }
            else {
                poolState = PoolState.SHUTDOWN_STARTED
                waitingThreads.forEach {
                    waiter -> waiter.hasWork.signal()
                }
            }
        }
    }

    @Throws(InterruptedException::class)
    fun awaitTermination(timeout: Duration): Boolean {
        mutex.withLock {
            if (poolState == PoolState.TERMINATED) return true
            var remaining = timeout.inWholeNanoseconds
            do {
                remaining = terminationDone.awaitNanos(remaining)
                if (poolState == PoolState.TERMINATED) return true
                if (remaining <= 0) return false
            } while(true)
        }
    }
    
    suspend fun awaitTerminationSuspend( )  {
        suspendCoroutine<Unit> { cont ->
            mutex.withLock {
                if (poolState === PoolState.TERMINATED) cont.resume(Unit)
                else {
                    terminationAwaiters.add(cont)
                }
            }
        }
    }
}