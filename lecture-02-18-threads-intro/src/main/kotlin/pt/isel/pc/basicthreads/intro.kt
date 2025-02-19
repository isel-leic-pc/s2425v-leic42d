package pt.isel.pc.basicthreads

import mu.KotlinLogging
import java.lang.Thread.sleep

val logger = KotlinLogging.logger {}

fun main() {
    logger.info("test started on thread ${Thread.currentThread().name}")
    val thread = Thread {
        sleep(2000)
        logger.info("message from new thread")
    }

    thread.start()
    logger.info("test terminated on thread thread ${Thread.currentThread().name}")
}