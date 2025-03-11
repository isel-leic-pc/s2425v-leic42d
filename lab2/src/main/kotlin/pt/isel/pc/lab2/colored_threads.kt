package pt.isel.pc.lab2


fun launchSomeThreads(concurrencyLevel: Int, action : (Int)->Unit ) : List<Thread> {
    val threads = (1 .. concurrencyLevel).map {id->
        Thread {
            action(id)
        }
    }
    threads.forEach {
        t -> t.start()
    }
    return threads
}

private fun main() {
    val NREDS = 5
    val NGREENS = 10

    val redThreads = launchSomeThreads(NREDS)  { id->
        repeat(10) { action->
            println("simulating some work done by red thread $id, action $action")
        }

    }
    val greenThreads = launchSomeThreads(NGREENS)  { id->
        repeat(10) { action ->
            println("simulating some work done by green thread $id, action $action")
        }
    }
    greenThreads.forEach {
        it.join()
    }
    redThreads.forEach {
        it.join()
    }
}