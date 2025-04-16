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
    private class Worker(val hasWork: Condition, var code: Runnable? ) {}
    
    // the state of the ThreadPool. Use it
    // on shutdown protocol, not done here
    enum class PoolState { ACTIVE, SHUTDOWN_STARTED, TERMINATED }
    
    private var poolState = PoolState.ACTIVE
    private var poolSize = 0
    private val mutex = ReentrantLock()
 
    private val requestQueue = LinkedList<Runnable>()
    private val waitingThreads = LinkedList<Worker>()
    
    /**
     * Execute the runnable in a safe way, avoiding worker thread
     * abnormal termination
     */
    private fun safeExec(worker : Worker) {
        try {
            worker.code?.run()
            worker.code = null
        }
        catch(e: Exception) {
            // just swallow the exception
            // in order to avoid abnormal thread termination
        }
    }
    
    /*
     * here should be inserted all the necessary worker termination code
     */
    private fun workerTermination() {
        poolSize--
    }
    
    /**
     * private function that contains the worker thread loop, used
     * to retrieve and process submitted runnables
     */
    private fun workerLoop(worker: Worker ) {
        while (true) {
            safeExec(worker)
            mutex.withLock {
                // fast path
                if (requestQueue.isNotEmpty()) {
                    worker.code = requestQueue.removeFirst()
                } else {
                    var remaining = keepAliveTime.inWholeNanoseconds
                    // waiting path
                    waitingThreads.add(worker)
                    while (true) {
                        if (remaining <= 0) {
                            // terminated due to inactivity
                            waitingThreads.remove(worker)
                            workerTermination()
                            return
                        }
                        remaining = worker.hasWork.awaitNanos(remaining)
                        if (worker.code != null) break;
                    }
                }
            }
        }
    }
    
    @Throws(RejectedExecutionException::class)
    fun execute(task: Runnable): Unit {
        mutex.withLock {
            if (poolState !== PoolState.ACTIVE) throw RejectedExecutionException()
            
            if (waitingThreads.isNotEmpty()) {
                // kernel style thread notification
                val waiter = waitingThreads.removeFirst()
                waiter.code = task
                waiter.hasWork.signal()
            }
            else if (poolSize < maxThreadPoolSize) {
                poolSize++
                Thread {
                    workerLoop(Worker(mutex.newCondition(), task))
                }.start()
            }
            else {
                requestQueue.add(task)
            }
        }
    }
}