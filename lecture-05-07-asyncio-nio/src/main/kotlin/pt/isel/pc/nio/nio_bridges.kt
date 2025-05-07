package pt.isel.pc.nio

import mu.KotlinLogging
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.TimeUnit

typealias IOContinuation<T> = (Throwable?, T) -> Unit


private val logger = KotlinLogging.logger {}

val IO_ERROR = -2
val EOF = -1


// Bridge to functional continuations

// files

val rwFileCompletion = object: CompletionHandler<Int, IOContinuation<Int>> {
    override fun completed(result: Int, cont: IOContinuation<Int>) {
        cont( null, result)
    }
    
    override fun failed(exc: Throwable?, cont: IOContinuation<Int>) {
        cont(exc, EOF)
    }
}

fun AsynchronousFileChannel.read(buffer: ByteBuffer, pos: Long, cont: IOContinuation<Int>) {
    read(buffer,pos, cont, rwFileCompletion)
}


fun AsynchronousFileChannel.write(buffer: ByteBuffer, pos: Long, cont: IOContinuation<Int>) {
    write(buffer,pos, cont, rwFileCompletion)
}

private object acceptCompleted : CompletionHandler<AsynchronousSocketChannel?,
                                        IOContinuation<AsynchronousSocketChannel?>> {
    override fun completed(result: AsynchronousSocketChannel?,
                           attach: IOContinuation<AsynchronousSocketChannel?>) {
        attach(null, result)
    }

    override fun failed(exc: Throwable, attach: IOContinuation<AsynchronousSocketChannel?>) {
        attach(exc, null)
    }
}

private object rwCompleted : CompletionHandler<Int, IOContinuation<Int>> {
    override fun completed(result: Int, attach: IOContinuation<Int>) {
        attach(null, result)
    }

    override fun failed(exc: Throwable, attach: IOContinuation<Int>) {
        attach(exc, IO_ERROR)
    }
}


fun AsynchronousServerSocketChannel.accept(cont: IOContinuation<AsynchronousSocketChannel?>) {
    this.accept(cont, acceptCompleted )
}

fun AsynchronousSocketChannel.read (dst: ByteBuffer, cont: IOContinuation<Int>) {
    this.read(dst, cont, rwCompleted)
}

fun AsynchronousSocketChannel.readTimeout (dst: ByteBuffer, tmMillis : Long, cont: IOContinuation<Int>) {
    this.read(dst, tmMillis, TimeUnit.MILLISECONDS, cont, rwCompleted)
}


fun AsynchronousSocketChannel.write (src: ByteBuffer, cont: IOContinuation<Int>) {
    this.write(src, cont, rwCompleted)
}







