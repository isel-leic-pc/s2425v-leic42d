package pt.isel.pc.echoservers

import mu.KotlinLogging
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket


class SimpleEchoServer(val port: Int) {
    companion object {
        val BACKLOG = 128
        val EXIT = "exit"
        val logger = KotlinLogging.logger {}
    }

    private fun BufferedWriter.writeLine(line: String) {
        appendLine(line)
        flush()
    }

    private fun processConnection(clientSock: Socket) {
        logger.info("client ${clientSock.remoteSocketAddress} connected")
        clientSock.use {
            val reader = BufferedReader(InputStreamReader(clientSock.getInputStream()))
            val writer = BufferedWriter(OutputStreamWriter(clientSock.getOutputStream()))

            while(true) {
                val line = reader.readLine() ?: break
                if (line == EXIT) {
                    writer.writeLine("bye")
                    break;
                }
                logger.info("line '$line' received")
                writer.writeLine(line)

            }
        }
    }

    fun run() {
        ServerSocket().use { serverSock ->
            serverSock.bind(InetSocketAddress("0.0.0.0", port))
            logger.info("Waiting for client connections")
            while(true) {
                val clientSock = serverSock.accept()
                Thread {
                    processConnection(clientSock)
                }.start()
            }
        }
    }
}

fun main() {
    SimpleEchoServer(8000).run()
}