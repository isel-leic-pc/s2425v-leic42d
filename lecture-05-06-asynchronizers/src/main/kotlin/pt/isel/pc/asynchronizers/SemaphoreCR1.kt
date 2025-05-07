package pt.isel.pc.asynchronizers

import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SemaphoreCR1(initialUnits : Int) {
    init {
        require(initialUnits >= 0)
    }
    private var permits = initialUnits
    private val lock = ReentrantLock()
    
    private class Request(val units: Int,
                          val cont : Continuation<Unit>)
    
    private val requests = LinkedList<Request>()
    
    /**
     * here we destructure lock acquire/release in order
     * to avoid enter suspendCoroutine
     * The eventual efficiency gain may not be above the loss of clarity
     */
    suspend fun acquire(units: Int) {
        // try fast path before enter suspendCoroutine
        lock.lock()
        if (units <= permits) {
            permits -= units
            lock.unlock()
            return
        }
        
        suspendCoroutine<Unit> {
                cont ->
            // suspend path
            requests.add(Request(units, cont))
            lock.unlock()
        }
    }
  
    
    private fun tryResolveRequests() : List<Request> {
        val resolved = mutableListOf<Request>()
        while(requests.isNotEmpty() &&
            requests.first().units <= permits) {
            val r = requests.removeFirst()
            permits -= r.units
            resolved.addLast(r)
        }
        return resolved
    }
    
    fun release(units: Int) {
        var resolved : List<Request>
        lock.withLock {
            permits+= units
            resolved = tryResolveRequests()
        }
        
        // resume resolved acquires
        for(r in resolved) {
            r.cont.resume(Unit)
        }
    }
}