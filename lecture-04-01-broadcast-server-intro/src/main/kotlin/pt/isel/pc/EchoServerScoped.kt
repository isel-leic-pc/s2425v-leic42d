package pt.isel.pc

import mu.KotlinLogging
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Semaphore

private val logger = KotlinLogging.logger {}

class EchoServerScoped(val port: Int) {
    companion object {
        val BACKLOG = 1024
        val EXIT = "exit"
        val MAX_CLIENTS = 10

    }

    val clientSockets = ClientSockets()
    val sessionsAvailable = Semaphore(MAX_CLIENTS)
    private val serverSocket = ServerSocket()
    private val serverScope = ThreadScope("serverScope", Thread.ofPlatform())

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

    fun newClient(socket: Socket, id: Int) {
        val childScope = serverScope.newChildScope("child $id");
        clientSockets.add(socket)
        childScope?.startThread {
            try {
                processConnection(socket, id)
            }
            finally {
                sessionsAvailable.release()
                clientSockets.remove(socket)
            }
        }
    }

    fun run() {
        logger.info("Waiting for connections...")

        serverScope.startThread {
            try {
                serverSocket.bind(InetSocketAddress("0.0.0.0", port), BACKLOG)
                serverSocket.use {
                    var clientId = 1
                    while (true) {
                        sessionsAvailable.acquire()
                        val clientSocket = serverSocket.accept()
                        newClient(clientSocket, clientId++)
                    }
                }
            } catch (e: IOException) {
                logger.info("run: exception ${e.message} occurred")
            }
        }
    }


    fun shutdown() {
        serverSocket.close()
        clientSockets.close()
        serverScope.cancel()
    }

    fun join() {
        serverScope.join()
    }
}

fun main() {
    val server =  EchoServerScoped(8000)
    // register shutdown hook
    val shutdownThread = Thread {
        //readln()
        logger.info("Starting shutdown process")
        server.shutdown()
        server.join()
    }
    //shutdownThread.start()
    Runtime.getRuntime().addShutdownHook(shutdownThread)

    server.run()
    logger.info("Waiting for server termination")
    server.join()
    logger.info("Terminated server")
}