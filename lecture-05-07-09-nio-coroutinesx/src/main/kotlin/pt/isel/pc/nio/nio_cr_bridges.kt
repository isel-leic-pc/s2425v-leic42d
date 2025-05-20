package pt.isel.pc.nio

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.charset.Charset


private val charSet = Charset.defaultCharset()
private val decoder = charSet.newDecoder()



suspend fun AsynchronousServerSocketChannel.acceptSuspend()
        : AsynchronousSocketChannel {
   TODO("to implement by students")
}

suspend fun AsynchronousSocketChannel.readSuspend (
    dst: ByteBuffer) : Int {
    TODO("to implement by students")
}

suspend fun AsynchronousSocketChannel.writeSuspend (
    dst: ByteBuffer) : Int {
    TODO("to implement by students")
}

private val CR = 13.toByte()
private val LF = 10.toByte()


suspend fun AsynchronousSocketChannel.readLine(): String? {
    val buffer = ByteBuffer.allocate(4096)
    val n =  readSuspend(buffer)
    
    if (n <= 0) return  null
    var pos = buffer.position()
    if (buffer.get(pos-1) == LF)
        buffer.position(--pos)
    if (pos > 0 && buffer.get(pos-1)  == CR)
        buffer.position(--pos)
    buffer.flip()
    return decoder.decode(buffer).toString()
}

private fun putBuffer(text: String, buffer: ByteBuffer) {
    buffer.clear()
    buffer.put(charSet.encode(text))
    buffer.put(CR)
    buffer.put(LF)
    buffer.flip()
}

suspend fun AsynchronousSocketChannel.writeLine(str: String)  {
    val buffer = ByteBuffer.allocate(4096)
    putBuffer(str, buffer)
    writeSuspend(buffer)
}