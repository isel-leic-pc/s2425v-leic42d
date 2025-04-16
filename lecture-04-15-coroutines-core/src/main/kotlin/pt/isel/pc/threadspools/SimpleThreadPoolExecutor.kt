package pt.isel.pc.threadspools


import java.util.*
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.*
import kotlin.time.Duration

class SimpleThreadPoolExecutor(
    private val maxThreadPoolSize: Int,
    private val keepAliveTime: Duration) {
    
    // node class for waiting workers list
    private class Waiter(val hasWork: Condition, var code: Runnable? ) {}
    
    // the state of the ThreadPool. Use it
    // on shutdown protocol, not done here
    enum class PoolState { ACTIVE, SHUTDOWN_STARTED, TERMINATED }
    
    private var poolState = PoolState.ACTIVE
    private var poolSize = 0
    private val mutex = ReentrantLock()
 
    private val requestQueue = LinkedList<Runnable>()
    private val waitingThreads = LinkedList<Waiter>()
    
    /**
     * Execute the runnable in a safe way, avois worker thread
     * abnormal termination
     */
    private fun safeExec(waiter : Waiter) {
        try {
            waiter.code?.run()
            waiter.code = null
        }
        catch(e: Exception) {
            // just swallow the exception
            // in order to avoid abnormal thread termination
        }
    }
    
    private fun workerTermination() {
        poolSize--
    }
    /**
     * private function that contains the worker thread loop
     * to retrieve and process submitted runnables
     */
    private fun workerLoop( waiter: Waiter ) {
        while (true) {
            safeExec(waiter)
            mutex.withLock {
                // fast path
                if (requestQueue.isNotEmpty()) {
                    waiter.code = requestQueue.removeFirst()
                } else {
                    var remaining = keepAliveTime.inWholeNanoseconds
                    // waiting path
                    waitingThreads.add(waiter)
                    while (true) {
                        if (remaining <= 0) {
                            // terminated due to inactivity
                            waitingThreads.remove(waiter)
                            workerTermination()
                            return
                        }
                        remaining = waiter.hasWork.awaitNanos(remaining)
                        if (waiter.code != null) break;
                    }
                }
            }
        }
    }
    
    @Throws(RejectedExecutionException::class)
    fun execute(task: Runnable): Unit {
        mutex.withLock {
            if (poolState !== PoolState.ACTIVE) throw RejectedExecutionException()
            
            // kernel style thread notification
            if (waitingThreads.isNotEmpty()) {
                val waiter = waitingThreads.removeFirst()
                waiter.code = task
                waiter.hasWork.signal()
            }
            else if (poolSize < maxThreadPoolSize) {
                poolSize++
                Thread {
                    workerLoop(Waiter(mutex.newCondition(), task))
                }
            }
            else {
                requestQueue.add(task)
            }
        }
    }
    
    
    
}