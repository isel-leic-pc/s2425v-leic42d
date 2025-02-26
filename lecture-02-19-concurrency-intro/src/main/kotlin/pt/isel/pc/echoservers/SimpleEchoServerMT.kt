package pt.isel.pc.echoservers

import mu.KotlinLogging
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY
import pt.isel.pc.basicthreads.writeLine
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket



class SimpleEchoServerMT(private val port: Int) {

    companion object {
        val EXIT = "exit"
        val BACKLOG = 1024
        private val logger = KotlinLogging.logger {}
    }

    private val serverSocket = ServerSocket()

    private fun processConnection(clientSock: Socket, clientId: Int ) {
        logger.info("client ${clientSock.remoteSocketAddress} connected")
        try {
            clientSock.use {
                val reader = BufferedReader(InputStreamReader(clientSock.getInputStream()))
                val writer = BufferedWriter(OutputStreamWriter(clientSock.getOutputStream()))
                writer.writeLine("Hello, client $clientId")

                while (true) {
                    val line = reader.readLine() ?: break
                    if (line == EXIT) {
                        writer.writeLine("Bye, client $clientId")
                        break;
                    }
                    logger.info("line '$line' received")
                    writer.writeLine(line)
                }
            }
        }
        catch(e: Exception) {
            logger.error("connection loop error: ${e.message}")
        }
        finally {
            logger.info("connection loop termination")
        }
    }

    fun run() {
        try {
            var clientId = 0
            serverSocket.use {
                serverSocket.bind(InetSocketAddress("0.0.0.0", port), BACKLOG)
                logger.info("waiting for client connections")
                while (true) {
                    val clientSock = serverSocket.accept()
                    val localClientId = ++clientId
                    // on first version the code was the next commented line
                    // processClient(clientSock, ++clientId )
                    // bad choice, not thread safe, increments can be lost,
                    // resulting in duplicated ids for clients
                    // try the error, reverting to old version and execute EchoServerMTTest
                    Thread {
                        processConnection(clientSock, localClientId)
                    }.start()
                }
            }
        }
        catch(e: Exception) {
            logger.info("accept loop error: ${e.message}")
        }
        finally {
            logger.info("accept loop termination")
        }
    }

    fun close() {
        serverSocket.close()
    }
}

fun main() {
    SimpleEchoServerMT(8000).run()
}