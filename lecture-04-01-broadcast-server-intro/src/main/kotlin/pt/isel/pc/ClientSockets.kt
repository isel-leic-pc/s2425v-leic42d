package pt.isel.pc

import java.net.Socket
import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ClientSockets {
    private val sockets = LinkedList<Socket>()
    private val mutex = ReentrantLock()
    fun add(socket: Socket) {
        mutex.withLock {
            sockets.add(socket)
        }

    }

    fun remove(socket: Socket) {
        mutex.withLock {
            sockets.remove(socket)
        }
    }

    fun close() {
        mutex.withLock {
            for(socket in sockets) {
                socket.close()
            }
        }
    }
}