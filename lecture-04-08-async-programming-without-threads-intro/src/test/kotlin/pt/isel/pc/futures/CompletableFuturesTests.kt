package pt.isel.pc.futures

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Future

class CompletableFuturesTests {
    private val logger = KotlinLogging.logger {}
    val executor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors())

    /**
     * Create a CompletableFuture already completed
     */
    private fun oper0(c: Int) : CompletableFuture<Int> =
        CompletableFuture.completedFuture(c + 1)


    /**
     * Create an complete a CompletableFuture programmatically
     */
    private fun oper01(c: Int) : CompletableFuture<Int> {
        val f =  CompletableFuture<Int>()
        f.complete(5)
        return f;

    }

   

    /**
     * Create and asynchronously  start a CompletableFuture
     * on default thread pool (ForkJoinPool)
     */
    private fun oper1Async(v: Int) : CompletableFuture<Int> =
        CompletableFuture.supplyAsync {
            logger.info("oper1Async start")
            sleep(2000)
            logger.info("oper1Async done")
            v + 1
        }

    /**
     * Using a runnable asynchronously executing in an arbitrary
     * thread pool, to vreate an async operation returning  a CompletableFuture
     * manually constructed and completed
     */
    private fun oper2Async(v: Int) : CompletableFuture<Int> {
        val resFut = CompletableFuture<Int>()

        executor.execute {
            logger.info("oper2Async start")
            sleep(4000)
            logger.info("oper2Async  done")
            resFut.complete(v * 2)
        }
        return resFut
    }
    

    @Test
    fun `use completable futures tasks as futures`() {

        val f1 =  oper1Async(1)
        val f2 =  oper2Async(2)

        var v1 = f1.get()
        var v2 = f2.get()

        assertEquals(2, v1)
        assertEquals(4, v2)
     
    }
    
    // Serial chains

    @Test
    fun `apply some transformation for future`() {
        val f1 =  oper1Async(1)

        val fs   = f1.thenApply { n ->
            Pair(n, n+1)
        }

        assertEquals(Pair(2,3), fs.join())
       
    }

    @Test
    fun `invoke some async operation on future completion`() {
        TODO()
       
    }


    // General combinators
    
    @Test
    fun `launch completable futures tasks and  combine it`() {
       TODO()
    }

}