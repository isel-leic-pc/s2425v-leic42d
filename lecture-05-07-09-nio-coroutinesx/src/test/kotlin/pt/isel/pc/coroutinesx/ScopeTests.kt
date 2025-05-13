package pt.isel.pc.coroutinesx

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals

import pt.isel.pc.nio.coroutinesx.getJobState
import kotlin.test.Test


class ScopeTests {
    companion object {
        private val logger = KotlinLogging.logger {}
    }
    
    @Test
    fun `first scope test`() {
         TODO()
    }

}