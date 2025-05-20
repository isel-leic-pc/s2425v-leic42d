/**
 * A version of the echo server using coroutines and
 * a suspend API to NIO2 socket channels
 */

package pt.isel.pc.nio.echo_servers

import mu.KotlinLogging
import pt.isel.pc.nio.acceptSuspend
import pt.isel.pc.nio.readLine
import pt.isel.pc.nio.writeLine
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.Executors
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

private val logger = KotlinLogging.logger {}

class EchoServerCR(private val port : Int) {
    val group = AsynchronousChannelGroup
        .withThreadPool(Executors.newSingleThreadExecutor())
    
    val serverSocketChannel = AsynchronousServerSocketChannel.open(group)
 
    /**
     * The server start launching the acceptLoop coroutine
     * In this implementation it is a regular (non suspend) function,
     * so it can be called by regular code.
     */
    fun start() {
        val completion = Continuation<Unit>(EmptyCoroutineContext) {}
        suspend { try {
                acceptLoop(serverSocketChannel)
            }
            catch (e: Throwable) {
                serverSocketChannel.close()
                logger.error("unrecoverable error in server:${e.message}")
            }
        }.startCoroutine(completion)
    }
    
    /**
     * The client session processing loop
     */
    suspend fun processClient(socket: AsynchronousSocketChannel, clientId: Int ) {
        logger.info("new session from ${socket.remoteAddress}")
        socket.writeLine("Hello, client $clientId")
        do {
         
            val line = socket.readLine()
            logger.info("new line from client $clientId")
            if (line == null || line.equals("exit")) {
                socket.writeLine("Bye")
                logger.info("terminate session from ${socket.remoteAddress}")
                break;
            }
            socket.writeLine(line)
        } while (true)
        socket.close()
    }
    
    private fun launchClient(clientChannel : AsynchronousSocketChannel, clientId:Int) {
        val completion = Continuation<Unit>(EmptyCoroutineContext) {}
        suspend {
            logger.info("client ${clientChannel.remoteAddress} connected")
          
            try {
                processClient(clientChannel, clientId)
            } catch (e: Throwable) {
                logger.error("unrecovered error in client:${e.message}")
            } finally {
                logger.info("client ${clientChannel.remoteAddress}  disconnected")
                clientChannel.close()
            }
        }.startCoroutine(completion)
    }

    suspend fun acceptLoop(serverChannel: AsynchronousServerSocketChannel) {
        serverChannel.bind(InetSocketAddress("0.0.0.0", port))
        var clientId = 1
        while (true) {
            val clientChannel = serverChannel.acceptSuspend()
            // launch the coroutine for client processing
            launchClient(clientChannel, clientId)
        }
    }
}

private fun main() {
    val server = EchoServerCR(8080)
    server.start()

    println("Press enter to shutdown...")
    readln()
}