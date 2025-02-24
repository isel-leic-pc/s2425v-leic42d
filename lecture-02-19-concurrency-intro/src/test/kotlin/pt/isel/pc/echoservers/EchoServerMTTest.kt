package pt.isel.pc.echoservers

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pt.isel.pc.echoclient.EchoClient
import java.lang.Thread.sleep
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class EchoServerTest {
    companion object {
        val nclients = 800
        private val logger = KotlinLogging.logger {}
    }

    @Test
    fun `check if there are no duplicate id's on multiple echo clients`() {
        // here we run the server in a thread, so we  check it
        // without external dependencies
        var server = SimpleEchoServerMT(8000)
        val serverThread = Thread {
            server.run()
            logger.info("Server termination")
        }
        serverThread.start()

        // Just sleep a bit to guarantee that server is already running the accept loop
        // Not a good synchronization method but sufficient for tests
        sleep(5000)

        // A thread safe collection from java libraries
        val ids = ConcurrentHashMap.newKeySet<Int>()
        val threads = mutableListOf<Thread>()
        repeat(nclients) {
             val thread =  Thread  {
                val client = EchoClient("127.0.0.1", 8000)
                val res = client.contact()
                if (!ids.add(res)) {
                    logger.warn("$res: duplicated id!")
                }
            }
            threads.add(thread)
            thread.start()
            // periodically wait a little to avoid refused connections
            // this is a behaviour of the TCP driver of the Operating System
            if (it % 50 ==0) sleep(3)
        }

        // waiting for clients termination
        for(t in threads) t.join()

        // force server termination by closing the socket
        server.close()

        // wait for server thread
        serverThread.join()
        assertEquals(nclients, ids.size)

    }


}