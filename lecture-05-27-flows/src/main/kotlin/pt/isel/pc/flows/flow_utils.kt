package pt.isel.pc.flows

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun factorialsFlow() : Flow<Long> = flow {
    var n = 0
    var f = 1L
    
    while(true) {
        logger.info("emit $f")
        emit(f)
        delay(1000)
        f = f* (++n)
    }
}

fun range(start: Int, end: Int, period: Long) : Flow<Int> {
    return flow<Int> {
        for(i in start until end) {
            logger.info("emit $i")
            this.emit(i)
            delay(period)
        }
    }
}

fun pairs() = flow<Int> {
    var curr = 2
    while(true) {
        delay(1000)
        emit(curr)
        curr += 2
    }
}

fun <T> flow2(block : suspend FlowCollector<T>.() -> Unit) : Flow<T> {
    return object : Flow<T> {
        override suspend fun collect(collector: FlowCollector<T>) {
            collector.block()
        }
        
    }
}

