package pt.isel.pc.coroutinesx.asynchronizers

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.Thread.sleep
import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * initial version without support for cancellation
 */
class SemaphoreCRC(initialUnits : Int) {
    init {
        require(initialUnits >= 0)
    }
    private var permits = initialUnits
    private val lock = ReentrantLock()
    
    private class Request(val units: Int) {
        var done = false
        var cont : Continuation<Unit>? = null
    }
    
    private val requests = LinkedList<Request>()
    
    fun currentPermits() : Int {
        lock.withLock {
            return permits
        }
    }
    
    suspend fun acquire(units: Int) {
        val request = Request(units)
        try {
            suspendCancellableCoroutine<Unit> { cont ->
                
                lock.withLock {
                    // try fast path
                    if (units <= permits) {
                        permits -= units
                        request.done = true
                        sleep(2000)
                        cont.resume(Unit)
                    } else {
                        // suspend path
                        request.cont = cont
                        requests.add( request)
                    }
                }
            }
        }
        catch(e : CancellationException) {
            lock.withLock {
                if (request.done) return
                requests.remove(request)
            }
        }
        
    }
    
    private fun tryResolveRequests() : List<Request> {
        val resolved = mutableListOf<Request>()
        while(requests.isNotEmpty() &&
            requests.first().units <= permits) {
            val r = requests.removeFirst()
            permits -= r.units
            r.done = true
            resolved.addLast(r)
        }
        return resolved
    }
    
    fun release(units: Int) {
        var resolved : List<Request>
        lock.withLock {
            permits += units
            resolved = tryResolveRequests()
        }
        
        // resume resolved acquires
        for(r in resolved) {
            r.cont?.resume(Unit)
        }
    }
}