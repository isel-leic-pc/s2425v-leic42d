package pt.isel.pc

import mu.KotlinLogging
import java.io.Closeable
import kotlin.time.Duration


class ThreadScope(private val name: String,
                  private val builder : Thread.Builder = Thread.ofPlatform(),
                  private val parentScope : ThreadScope? = null) : Closeable {

    companion object {
        private val logger = KotlinLogging.logger {}
    }


    // Creates a new thread in the scope, if the scope is not closed.
    fun startThread(runnable: Runnable): Thread? {
       TODO()
    }

    // Creates a new child scope, if the current scope is not closed.
    fun newChildScope(name: String): ThreadScope? {
      TODO()
    }

    // Closes the current scope, disallowing the creation of any further thread
    // or child scope.
    override fun close() {
        TODO()
    }

    // Waits until all threads and child scopes have completed
    @Throws(InterruptedException::class)
    fun join(timeout: Duration = Duration.INFINITE): Boolean {
      TODO()
    }

    // Interrupts all threads in the scope and cancels all child scopes.
    fun cancel() {
         TODO()
    }

}