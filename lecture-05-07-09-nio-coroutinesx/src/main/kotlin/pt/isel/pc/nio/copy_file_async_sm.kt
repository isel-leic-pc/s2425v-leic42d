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

class CopyFileAsyncSM2(val pathSrc: String, val pathDst: String) {
    
    companion object {
        private val tls = ThreadLocal<MemoSynch?>()
    }
    lateinit var  buffer: ByteBuffer
    lateinit var  inputFile : AsynchronousFileChannel
    lateinit var  outputFile : AsynchronousFileChannel
    lateinit var completion : CompletionHandler<Long, Void?>
    var pos = 0L
    var syncReadCounter = 0
    var syncWriteCounter = 0
    private class MemoSynch {
        var sync = false
        var nextState : State? = null
    }
    
    sealed interface State {
        data object StartState : State
        data object ReadBlockState : State
        data class BlockReadState(val readBytes: Int) : State
        data object WriteBlockState : State
        data class WrittenBlockState(val writtenBytes: Int) : State
        data object DoneState : State
        data class FailureState(val err: Throwable) : State
        data object Suspend : State
    }
    
    private fun start() : State {
        buffer = ByteBuffer.allocate(1)
        inputFile = AsynchronousFileChannel.open(Path.of(pathSrc), StandardOpenOption.READ)
        outputFile = AsynchronousFileChannel.open(
            Path.of(pathDst),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )
        return State.ReadBlockState
    }
    
    private fun readBlock(pos: Long) : State {
        val completion = object: CompletionHandler<Int, Void? > {
            override fun completed(result: Int, attach: Void?) {
                val memoSync = tls.get()
                if (memoSync != null) {
                    memoSync.sync = true
                    memoSync.nextState = State.BlockReadState(result)
                }
                else {
                    runLoop(State.BlockReadState(result) )
                }
            }
            
            override fun failed(exc: Throwable, attach: Void?) {
                val memoSync = tls.get()
                if (memoSync != null) {
                    memoSync.sync = true
                    memoSync.nextState = State.FailureState(exc)
                }
                else {
                    runLoop(State.FailureState(exc))
                }
            }
        }
        
        buffer.clear()
        val memoSynch = MemoSynch()
        tls.set(memoSynch)
        inputFile.read(buffer,pos, null, completion)
        tls.set(null)
        if (memoSynch.sync) {
            syncReadCounter++
            return memoSynch.nextState!!
        }
       
        return State.Suspend
    }
    
    private fun blockRead(bytesRead: Int) : State {
        if (bytesRead == -1) {
            logger.info("done")
            completion.completed(pos, null)
            return State.DoneState
        }
        buffer.flip()
        return State.WriteBlockState
    }
    
    private fun writeBlock(pos: Long) : State{
        val completion = object: CompletionHandler<Int, Void? > {
            override fun completed(result: Int, attach: Void?) {
                val memoSync = tls.get()
               
                if (memoSync != null) {
                    memoSync.sync = true
                    memoSync.nextState = State.WrittenBlockState(result)
                }
                else {
                    runLoop(State.WrittenBlockState(result))
                }
            }
            
            override fun failed(exc: Throwable, attach: Void?) {
                val memoSync = tls.get()
               
                if (memoSync != null) {
                    memoSync.sync = true
                    memoSync.nextState = State.FailureState(exc)
                }
                else {
                    runLoop(State.FailureState(exc))
                }
            }
        }
        val memoSynch = MemoSynch()
        tls.set(memoSynch)
        
        outputFile.write(buffer,pos, null, completion)
        tls.set(null)
        if (memoSynch.sync) {
            syncWriteCounter++
            return memoSynch.nextState!!
        }
        return State.Suspend
    }
    
    private fun writtenBlock(writtenBytes: Int) : State{
        pos += writtenBytes
        if (buffer.position() == buffer.limit()) {
            buffer.clear()
            return State.ReadBlockState
        }
        else {
            return State.WriteBlockState
        }
    }
    
    private fun failure(error: Throwable) : State {
        safeClose(inputFile)
        safeClose(outputFile)
        completion.failed(error, null)
        return State.DoneState
    }
    
    
    private fun runLoop(state: State) {
        var nextState = state
        while(true) {
            try {
                when (nextState) {
                    State.StartState -> nextState = start()
                    State.ReadBlockState -> nextState = readBlock(pos)
                    is State.BlockReadState -> nextState = blockRead(nextState.readBytes)
                    State.Suspend -> break
                    State.DoneState -> break
                    State.WriteBlockState -> nextState = writeBlock(pos)
                    is State.WrittenBlockState -> {
                        nextState = writtenBlock(nextState.writtenBytes)
                    }
                    is State.FailureState -> {
                        nextState = failure(nextState.err)
                    }
                }
            }
            catch( e: Exception) {
                if (state !is  State.FailureState) {
                    nextState = failure(e)
                } else {
                    completion.failed(e, null)
                    break
                }
            }
        }
    }
    
    
    fun runLoop(completion: CompletionHandler<Long, Void?>) {
        this.completion = completion
        runLoop(State.StartState)
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
    val cf = CopyFileAsyncSM2("dud_en.txt", "dud_en_out.txt")
    cf.runLoop(completion)
    
    cdl.await()
    logger.info("done with ${cf.syncReadCounter} synchronous reads and ${cf.syncWriteCounter} writes")
}

