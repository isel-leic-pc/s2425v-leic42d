package pt.isel.pc.flows

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import pt.isel.pc.sequences.SequencesAndIterablesTests
import pt.isel.pc.sequences.factorials
import kotlin.test.Test

class FlowsTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    
    @Test
    fun `factorial flow test`() {
        var flowFact10 = factorialsFlow()
            .flowOn(Dispatchers.IO)
            .take(10)
        
        logger.info("before consume")
        
        runBlocking {
            flowFact10.collect {
                logger.info("received $it")
            }
            
//            flowFact10.collect {
//                logger.info("received $it")
//            }
        }
        
        
    }
    
    
    @Test
    fun `first ten evens`() {
        val evens = flow {
            repeat(10) {
                emit((it +1)*2)
                delay(1000)
            }
        }
        runBlocking {
            evens
            .take(4)
            .map {
                it + 1
            }
            .collect {
                println(it)
            }
        }
       
    }
    
   
    
    
    // flowOn
    @Test
    fun `flow on a certain context`() {
        runBlocking {
            range(10,20, 2000)
                .flowOn(Dispatchers.IO)
                .map {
                        it ->
                    logger.info("map $it")
                    it*2
                }
                .collect {
                    logger.info("collect $it")
                }
        }
        logger.info("end test")
    }
    
    // flat map
    
    @Test
    fun `concurrency with flatMapConcat (sequential)`() {
        runBlocking {
            range(1,20, 1000).flatMapConcat {
                    i -> range(i*20, i*20+20, 500)
            }
            .collect {
                logger.info("$it")
            }
        }
        
    }
    
    @Test
    fun `concurrency with flatMapMerge (concurrent)`() {
        runBlocking {
            range(1,20, 1000).flatMapMerge(2) {
                    i -> range(i*20, i*20+10, 500)
            }
            .collect {
                logger.info("$it")
            }
        }
        
    }
    
    @Test
    fun `concurrency with flatMapLatest`() {
        runBlocking {
            range(1,20, 1000).flatMapLatest {
                    i -> range(i*20, i*20+20, 500)
            }
                .collect {
                    logger.info("$it")
                }
        }
        
    }
    
    // flow synchronous implementation

//    fun interface FlowCollector<T> {
//        suspend fun emit(t: T);
//    }
//
//    interface Flow<T> {
//        suspend fun collect(collector: FlowCollector<T>)
//    }
    
    private  fun <T> collector(emitter : suspend (t: T) -> Unit) : FlowCollector<T>  = FlowCollector {
            t -> emitter(t)
    }
    
    private fun <T> flow2(block : suspend FlowCollector<T>.() -> Unit) : Flow<T>{
        return object : Flow<T> {
            override suspend fun collect(collector: FlowCollector<T>) {
                collector.block()
            }
        }
    }
    
}