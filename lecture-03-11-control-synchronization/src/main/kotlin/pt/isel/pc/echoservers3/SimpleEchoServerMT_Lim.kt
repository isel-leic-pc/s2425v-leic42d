package pt.isel.pc.echoservers3

import mu.KotlinLogging
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicLong


class SimpleEchoServerMT_Lim(val port: Int) {
    companion object {
        val BACKLOG = 1024
        val EXIT = "exit"
        val MAX_CLIENTS = 2
        private val logger = KotlinLogging.logger {}
    }

    private val serverSocket = ServerSocket()

    fun BufferedWriter.writeLine(line: String) {
        appendLine(line)
        flush()
    }

    /**
     * * @param clientSock the socket connected to the client
     */
    private fun processConnection(clientSock: Socket, clientId: Int) {
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
                        break
                    }
                    //logger.info("line '$line' received")
                    writer.writeLine(line)
                }
            }
        }
        catch(e: IOException) {
            logger.info("processConnection: exception ${e.message} occurred")
        }
    }

    fun run() {
        logger.info("Waiting for connections...")

        try {
            serverSocket.bind(InetSocketAddress("0.0.0.0", port), BACKLOG)

            serverSocket.use {
                var clientId = 1
                while (true) {

                    val clientSocket = serverSocket.accept()
                    val newClientId = clientId++
                    Thread {
                        processConnection(clientSocket, newClientId)
                    }.start()
                }
            }
        } catch (e: Exception) {
            logger.info("run: exception ${e.message} occurred")
        }
    }


    fun close() {
        serverSocket.close()
    }
}

fun main() {
    SimpleEchoServerMT_Lim(8000).run()
}