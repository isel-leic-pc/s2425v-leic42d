package pt.isel.pc.sequences

import kotlinx.coroutines.delay
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class FactIterator : Iterator<Long> {
    var n = 0
    var f = 1L
    
    override fun next(): Long {
        logger.info("next n=$n, f=$f")
        val next = f
     
        f = f* (++n)
        Thread.sleep(500)
        return next
    }
    
    override fun hasNext(): Boolean = true
    
}

fun factorials() : Iterable<Long> {
    return object : Iterable<Long> {
        override fun iterator(): Iterator<Long> =
                         FactIterator()
    }
}

fun factorialsSeq() : Sequence<Long> = sequence {
    var n = 0
    var f = 1L
    
    while(true) {
        logger.info("yield $f")
        yield(f)
        //delay(1000) /* not possible */
        f = f* (++n)
    }
}