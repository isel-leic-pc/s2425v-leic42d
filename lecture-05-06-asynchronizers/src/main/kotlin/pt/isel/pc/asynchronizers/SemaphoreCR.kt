package pt.isel.pc.asynchronizers

import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * initial version without support for cancellation
 */
class SemaphoreCR(initialUnits : Int) {
    init {
        require(initialUnits >= 0)
    }
    private var permits = initialUnits
    private val lock = ReentrantLock()

    private class Request(val units: Int,
                          val cont : Continuation<Unit>)

    private val requests = LinkedList<Request>()

    suspend fun acquire(units: Int) {
        suspendCoroutine<Unit> {
            cont ->
           
            lock.withLock {
                // try fast path
                if (units <= permits) {
                    permits -= units
                    cont.resume(Unit)
                }
                else {
                    // suspend path
                    requests.add(Request(units, cont))
                }
            }
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
            permits += units
            resolved = tryResolveRequests()
        }

        // resume resolved acquires
        for(r in resolved) {
            r.cont.resume(Unit)
        }
    }
}