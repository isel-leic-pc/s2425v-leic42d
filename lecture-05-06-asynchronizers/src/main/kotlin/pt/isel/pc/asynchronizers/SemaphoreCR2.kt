package pt.isel.pc.asynchronizers

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.LinkedList
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * In this implementation we use a kotlin mutex
 */
class SemaphoreCR2(initialUnits : Int) {
    init {
        require(initialUnits >= 0)
    }
    private var permits = initialUnits
    private val lock = Mutex()
    
    private class Request(val units: Int,
                          val cont : Continuation<Unit>)
    
    private val requests = LinkedList<Request>()
    
    /**
     * here we HAVE to destructure lock acquire/release
     * because the block passed to suspendCoroutine is not suspend
     * and we
     * to avoid enter suspendCoroutine
     * The eventual efficiency gain may not be above the loss of clarity
     */
    suspend fun acquire(units: Int) {
        // try fast path before enter suspendCoroutine
        lock.lock()
        if (permits > units) {
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
    
    /**
     * Note that here we have to convert the release operation
     * to a suspend one, in order to support the use of kotlin mutex
     * For all these constraints I always ReentrantLock in this scenario
     */
    suspend fun release(units: Int) {
        var resolved : List<Request>
        lock.withLock {
            permits += units
            resolved = tryResolveRequests()
        }
        
        // resume resolved acquires
        for(r in resolved) {
            r.cont.resume(Unit)
        }
    }
}