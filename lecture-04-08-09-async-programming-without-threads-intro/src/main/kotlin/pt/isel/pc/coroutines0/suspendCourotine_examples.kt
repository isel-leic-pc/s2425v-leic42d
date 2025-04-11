package pt.isel.pc

import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


private val logger = KotlinLogging.logger {}
private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor()


private suspend fun delay2(millis: Long) {
   suspendCoroutine<Unit> {
       cont ->
       

   }
}



