package pt.isel.pc.nio

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.nio.charset.Charset
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private val charSet = Charset.defaultCharset()
private val decoder = charSet.newDecoder()
private val encoder = charSet.newEncoder()

private object acceptCompletedCR : CompletionHandler<AsynchronousSocketChannel,
        Continuation<AsynchronousSocketChannel>> {
    override fun completed(result: AsynchronousSocketChannel,
                           attach: Continuation<AsynchronousSocketChannel>
    ) {
        attach.resume(result)
    }
    
    override fun failed(exc: Throwable, attach: Continuation<AsynchronousSocketChannel>) {
        attach.resumeWithException(exc)
    }
}

private object rwCompletedCR : CompletionHandler<Int, Continuation<Int>> {
    override fun completed(result: Int, attach: Continuation<Int>) {
        attach.resume(result)
    }
    
    override fun failed(exc: Throwable, attach: Continuation<Int>) {
        attach.resumeWithException(exc)
    }
}

suspend fun AsynchronousServerSocketChannel.acceptSuspend()
        : AsynchronousSocketChannel {
    return suspendCoroutine<AsynchronousSocketChannel> {
        cont ->
        accept(cont,acceptCompletedCR)
    }
}

suspend fun AsynchronousSocketChannel.readSuspend (
    dst: ByteBuffer) : Int {
    return suspendCoroutine<Int> {
            cont ->
        read(dst, cont,rwCompletedCR)
    }
}

suspend fun AsynchronousSocketChannel.writeSuspend (
    dst: ByteBuffer) : Int {
    return suspendCoroutine<Int> {
            cont ->
        write(dst, cont,rwCompletedCR)
    }
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