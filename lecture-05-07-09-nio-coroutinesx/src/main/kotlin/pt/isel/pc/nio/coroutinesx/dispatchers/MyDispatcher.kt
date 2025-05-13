package pt.isel.pc.nio.coroutinesx.dispatchers

import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

class MyDispatcher : ContinuationInterceptor {
    companion object Key: CoroutineContext.Key<MyDispatcher>
   
    val pool = ThreadPoolExecutor(2, 60.seconds)
    
    override val key: CoroutineContext.Key<*>
        get() = MyDispatcher
    
    
    override  fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return object : Continuation<T> {
            override val context: CoroutineContext
                get() = continuation.context
            
            override fun resumeWith(result: Result<T>) {
                pool.execute(continuation as Continuation<Unit>)
            }
        }
    }
    
    override fun toString(): String = "Dispatcher(MyDispatcher)"
}