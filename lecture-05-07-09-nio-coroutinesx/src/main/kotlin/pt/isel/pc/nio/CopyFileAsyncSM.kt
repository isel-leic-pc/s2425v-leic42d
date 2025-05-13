package pt.isel.pc.nio

import mu.KotlinLogging
import pt.isel.pc.nio.FileUtils.Companion.safeClose
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.channels.CompletionHandler
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.concurrent.CountDownLatch

private val logger = KotlinLogging.logger {}

class CopyFileAsyncSM(val pathSrc: String, val pathDst: String) {
    val BUFFER_SIZE = 1
    lateinit var  buffer: ByteBuffer
    lateinit var  inputFile : AsynchronousFileChannel
    lateinit var  outputFile : AsynchronousFileChannel
    lateinit var completion : CompletionHandler<Long, Void?>
    var pos = 0L
    
    sealed interface State {
        data object Start : State
        data object ReadBlock : State
        data class BlockRead(val readBytes: Int) : State
        data object WriteBlock : State
        data class WrittenBlock(val writtenBytes: Int) : State
        data object Done : State
        data class Failure(val err: Throwable) : State
        data object Suspend : State
    }
    
    private fun start() : State {
        buffer = ByteBuffer.allocate(BUFFER_SIZE)
        inputFile = AsynchronousFileChannel.open(Path.of(pathSrc), StandardOpenOption.READ)
        outputFile = AsynchronousFileChannel.open(
            Path.of(pathDst),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )
        return State.ReadBlock
    }
    
    private fun readBlock(pos: Long) : State {
        val completion = object: CompletionHandler<Int, Void? > {
            override fun completed(result: Int, attach: Void?) {
                run(State.BlockRead(result) )
            }
            
            override fun failed(exc: Throwable, attach: Void?) {
                run(State.Failure(exc))
            }
        }
        
        buffer.clear()
        inputFile.read(buffer,pos, null, completion)
        return State.Suspend
    }
    
    private fun blockRead(bytesRead: Int) : State {
        if (bytesRead == -1) {
            logger.info("done")
            completion.completed(pos, null)
            return State.Done
        }
        buffer.flip()
        return State.WriteBlock
    }
    
    private fun writeBlock(pos: Long) : State{
        val completion = object: CompletionHandler<Int, Void? > {
            override fun completed(result: Int, attach: Void?) {
                run(State.WrittenBlock(result))
            }
            
            override fun failed(exc: Throwable, attach: Void?) {
                run(State.Failure(exc))
            }
        }
        
        outputFile.write(buffer,pos, null, completion)
        return State.Suspend
    }
    
    private fun writtenBlock(writtenBytes: Int) : State{
        pos += writtenBytes
        if (buffer.position() == buffer.limit()) {
            buffer.clear()
            return State.ReadBlock
        }
        else {
            return State.WriteBlock
        }
    }
    
    private fun failure(error: Throwable) : State {
        safeClose(inputFile)
        safeClose(outputFile)
        completion.failed(error, null)
        return State.Done
    }
    
    private fun run(nextState: State) {
        var state = nextState
        while(true) {
            try {
                when (state) {
                    State.Start -> state = start()
                    State.ReadBlock -> state = readBlock(pos)
                    is State.BlockRead -> state = blockRead(state.readBytes)
                    State.Suspend -> break
                    State.Done -> break
                    State.WriteBlock -> state = writeBlock(pos)
                    is State.WrittenBlock -> {
                        state = writtenBlock(state.writtenBytes)
                    }
                    is State.Failure -> {
                        state = failure(state.err)
                    }
                }
            }
            catch( e: Exception) {
                if (nextState !is  State.Failure) {
                    state = failure(e)
                } else {
                    completion.failed(e, null)
                    break
                }
            }
        }
    }
    
    fun run(completion: CompletionHandler<Long, Void?>) {
        this.completion = completion
        run(State.Start)
    }
}

fun main() {
    val cdl = CountDownLatch(1)
    val completion = object: CompletionHandler<Long, Void? > {
        override fun completed(result: Long, attach: Void?) {
            logger.info("file copied with size $result")
            cdl.countDown()
            
        }
        
        override fun failed(exc: Throwable, attach: Void?) {
            logger.info("error $exc copying file")
            cdl.countDown()
        }
    }
    val cf = CopyFileAsyncSM("dud_en.txt", "dud_en_out.txt")
    cf.run(completion)
    
    cdl.await()
    logger.info("done")
}