package pt.isel.pc.sequences

import mu.KotlinLogging
import mu.KotlinLogging.logger
import org.junit.jupiter.api.Test

class SequencesAndIterablesTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    
    @Test
    fun `factorial iterable test`() {
        var f10 = factorials()
        
        logger.info("before consume")
        for(f in f10) {
            println(f)
        }
    }
    
    @Test
    fun `factorial sequence test`() {
        var f10 = factorialsSeq()
            .take(10)
        logger.info("before consume")
        for(f in f10) {
            println(f)
        }
    }
}