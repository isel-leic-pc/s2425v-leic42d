package pt.isel.pc.nio.echo_servers

import mu.KotlinLogging
import pt.isel.pc.nio.accept
import pt.isel.pc.nio.read
import pt.isel.pc.nio.write
import java.io.BufferedWriter
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousServerSocketChannel

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import kotlin.apply
import kotlin.jvm.javaClass


class EchoServerCB(private val port: Int) {
    private val logger = KotlinLogging.logger {}

    private val BUF_SIZE  = 1024
    private val exitCmd = "exit\n"
    private val helloMsg = "hello, client "
    private val byeMsg = "bye\n"
    private val charSet = Charsets.UTF_8
    private val decoder = charSet.newDecoder()
    
    fun closeConnection(connectionChannel : AsynchronousSocketChannel) {
        try {
            logger.info("client ${connectionChannel.remoteAddress} disconnected")
            connectionChannel.close()
        }
        catch(e: IOException) {
        }
    }
    
    fun BufferedWriter.writeLine(format: String, vararg values : Any?) {
        write(String.format(format, *values))
        newLine()
        flush()
    }
    /**
     * The server accept and process loop
     */
    fun run() {

        //val group = AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor())
        val servSocket = AsynchronousServerSocketChannel.open()
        println("provider: ${servSocket.provider().javaClass.name}")
        servSocket.bind(InetSocketAddress("0.0.0.0", port))
        var clientId = 1

        fun acceptLoop() {
            servSocket.accept() { err, client ->
                if (err != null) {
                    logger.error("error on accept")
                    servSocket.close()
                }
                else {
                    logger.info("new client accepted from ${client?.remoteAddress}")

                    client?.apply { processClient(this, clientId++)}
                    acceptLoop()
                }
            }
        }

        acceptLoop()
    }

    /**
     * The client session processing loop
     */
    fun processClient(client: AsynchronousSocketChannel,  id: Int) {
        val buffer = ByteBuffer.allocate(BUF_SIZE)

        fun isExitCmd() : Boolean{
            val text = decoder.decode(buffer).toString()
            buffer.rewind()
            return text.equals(exitCmd)
        }

        fun putBuffer(text: String) {
            buffer.clear()
            buffer.put(charSet.encode(text))
            buffer.flip()
        }
        
        fun bye() {
            putBuffer(byeMsg)
            client.write(buffer) { err, res ->
                buffer.clear()
                client.shutdownInput()
                Thread.sleep(1000)
                logger.info("terminate client")
                client.close()
            }
        }

        fun clientLoop() {
            client.read(buffer) { err, nBytes ->
                if (err != null || nBytes < 0 ) {
                    closeConnection(client)
                } else {
                    logger.info("$nBytes received from ${client.remoteAddress}")
                    buffer.flip()
                    if (isExitCmd()) {
                        bye()
                    } else {
                        client.write(buffer) { err, nbytes ->
                            buffer.clear()
                            clientLoop()
                        }
                    }
                }
            }
        }
        
        fun start() {
            val hello = helloMsg + id + "\n"
            putBuffer(hello)
            client.write(buffer) { err, nbytes ->
                buffer.clear()
                clientLoop()
            }
        }
        start()
    }
}

private fun main() {
    val server = EchoServerCB(8080)
    server.run()
    println("press return to terminate...")
    readln()
}
