package pt.isel.pc.basicthreads

import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis


fun search(values: Array<String>, ref: String) : Int {
    var total = 0
    for (i in 0..values.size-1)
        if (ref.equals(values[i]))
            total++
    return total
}

fun psearch(values: Array<String>, ref: String) : Int {
    val nParts = Runtime.getRuntime().availableProcessors()
    val parts = partsList(values.size, nParts)

    val partialResults = IntArray(nParts) {0}

    val threads = parts.mapIndexed {
        resIndex, part ->
        thread {
            var count =0;
            for (idx in part.first until part.second) {
                if (values[idx].equals(ref))
                    count++
            }
            partialResults[resIndex] = count
        }
    }
    threads.forEach {
        thread -> thread.join()
    }
    return partialResults.sum()
}

fun test( function : (Array<String>, String) -> Int,
          values: Array<String>,
          ref: String, prefix: String) {
    var minTime = Long.MAX_VALUE
    var total = 0
    repeat(5) {
        var curTotal : Int
        val time = measureTimeMillis{
            curTotal =  function(values, ref)
        }
        if (time < minTime) {
            minTime = time
            total = curTotal
        }
    }

    println("$prefix found $total in ${minTime} ms!")
}

private fun buildString() : String {
    val sb = StringBuilder()
    repeat(30000) {
        sb.append('a' + (it % ('z' -'a'+1)))
    }
    return sb.toString()
}


private fun main() {
    val s = buildString()
    val sRef = buildString()
    val values = Array<String>(2_000_000) { s }

    println("Start sequential search")
    test(::search, values, sRef, "serial" )
    println("Start parallel search")
    test(::psearch, values, sRef, "parallel" )
}