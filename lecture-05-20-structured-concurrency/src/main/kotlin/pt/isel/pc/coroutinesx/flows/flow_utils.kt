package pt.isel.pc.coroutinesx.flows

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

fun <T> flow2(block: suspend FlowCollector<T>.() -> Unit): Flow<T> =
    object :Flow<T> {
        override suspend fun collect(c: FlowCollector<T>) {
            c.block()
        }
    }

suspend fun  <T,U> Flow<T>.map(mapper : (t: T) -> U) : Flow<U>  {
    return flow2 {
        collect {
            emit(mapper(it))
        }
    }
    
}

fun <T> Flow<T>.limit(lim: Int) : Flow<T>  {
    return flow2 {
                var i = 0
                try {
                    collect {
                        if (i++ < lim)
                            emit(it)
                        else throw CancellationException()
                    }
                }
                catch(e: CancellationException) {
                
                }
            }
    
}



