package pt.isel.pc

import mu.KotlinLogging
import pt.isel.pc.coroutines1.delay
import kotlin.coroutines.*

private val logger = KotlinLogging.logger {}


private suspend fun  fs(msg: String, millis: Long) : Long {
    
    logger.info("start fs with $msg")
    delay(millis)
    logger.info("end fs")
    return 23L
}

val fsn = ::fs as (String, Long, Continuation<Long>) -> Any

fun fsAsNormal() {
    val cont = object :Continuation<Long> {
        override val context: CoroutineContext
            get() = EmptyCoroutineContext
        
        override fun resumeWith(result: Result<Long>) {
            logger.info("result = $result")
        }
    }
    val res = fsn("hello", 2000, cont);
    logger.info("res=$res")
}

private fun main() {
   
    
    val completion = object: Continuation<Unit> {
        override val context: CoroutineContext
            get() = EmptyCoroutineContext
        
        override fun resumeWith(result: Result<Unit>) {
            logger.info("result = $result")
        }
    }
    
    suspend { fsAsNormal() }.startCoroutine(completion)
    
    logger.info("terminate")
}