package pt.isel.pc.nio

import mu.KotlinLogging
import pt.isel.pc.nio.FileUtils.Companion.copyFileAsync
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousChannel
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.CompletionHandler
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.CountDownLatch


private val logger = KotlinLogging.logger {}

class FileUtils {
    companion object {
      
        val BUFFER_SIZE = 128
        
        fun safeClose(channel: AsynchronousChannel) {
            try {
                channel.close()
            }
            catch(e: IOException) {
            
            }
        }
        
        fun copyFileAsync(pathSrc: String, pathDst: String, cont: IOContinuation<Long>) {
            
            lateinit var  buffer: ByteBuffer
            lateinit var  inputFile : AsynchronousFileChannel
            lateinit var  outputFile : AsynchronousFileChannel
            
            val rwCompletion = object: CompletionHandler<Int, IOContinuation<Int>> {
                override fun completed(result: Int, cont: IOContinuation<Int>) {
                    cont(result, null)
                }
                
                override fun failed(exc: Throwable?, cont: IOContinuation<Int>) {
                   cont(-1, exc)
                }
            }
            
            fun done(result: Long) {
                safeClose(inputFile)
                safeClose(outputFile)
                cont(result, null)
            }
            
            fun failure(error: Throwable) {
                safeClose(inputFile)
                safeClose(outputFile)
                cont(0, error)
            }
            
            fun readBlock(pos: Long, cont: IOContinuation<Int>) {
                buffer.clear()
                inputFile.read(buffer,pos, cont, rwCompletion)
            }
            
            fun writeBlock(pos: Long, cont: IOContinuation<Int>) {
                outputFile.write(buffer,pos, cont, rwCompletion)
            }
            
            fun writeAll(pos: Long, cont: IOContinuation<Int>) {
                fun partialWrite(pos: Long) {
                    writeBlock(pos) { count, err ->
                        if (err == null) {
                            if (buffer.position() == buffer.limit()) {
                                cont(buffer.limit(), null)
                            } else {
                                partialWrite(pos + count)
                            }
                        }
                        else {
                            failure(err)
                        }
                    }
                }
                partialWrite(pos)
            }
            
            fun transferNext(pos: Long) {
                readBlock(pos) {
                    count, err->
                    if (err == null) {
                        if (count == -1) {
                            done(pos)
                        }
                        else {
                            logger.info("read $count bytes at position($pos)")
                            buffer.flip()
                            writeAll(pos) {
                                count, err ->
                                if (err != null) {
                                   failure(err)
                                } else {
                                    logger.info("write $count bytes at position($pos)")
                                    transferNext(pos + count)
                                }
                            }
                        }
                    }
                    else {
                        failure(err)
                    }
                }
            }
            
            fun start() {
                try {
                    buffer = ByteBuffer.allocate(BUFFER_SIZE)
                    inputFile = AsynchronousFileChannel.open(Path.of(pathSrc), StandardOpenOption.READ)
                    outputFile = AsynchronousFileChannel.open(
                        Path.of(pathDst),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE
                    )
                    transferNext(0)
                }
                catch(e: Exception) {
                    failure(e)
                }
            }
            
            start()
            
        }
        
    }
}

fun main() {
    val cdl = CountDownLatch(1)
    copyFileAsync("dud_en.txt", "dud_en_out.txt") {
  
            result,err ->
        logger.info("file copied with size $result")
        cdl.countDown()
        
    }
    
    cdl.await()
    logger.info("done")
}