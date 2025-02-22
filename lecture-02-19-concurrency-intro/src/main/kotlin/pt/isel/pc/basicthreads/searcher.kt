package pt.isel.pc.basicthreads

/**
 * Searches sequentially through an array of elements and counts how many satisfy the provided predicate.
 *
 * @param T the type of elements in the array
 * @param values the array of elements to be searched
 * @param pred the predicate function that determines whether an element satisfies the condition
 * @return the number of elements within the array that satisfy the predicate
 */
fun <T>search(values: Array<T>, pred: (T) -> Boolean) : Int {
    var total = 0
    for (i in 0..values.size-1)
        if (pred(values[i]))
            total++
    return total
}

/**
 * A generic parallel search over an array of elements, counting the number of elements
 * that satisfy the given predicate.
 * The array is divided among multiple threads to improve performance.
 *
 * @param T the type of elements in the array
 * @param values the array of elements to be searched
 * @param pred the predicate function used to test each element of the array
 * @return the number of elements in the array that satisfy the predicate
 */
fun <T> psearch(values: Array<T>, pred: (T) -> Boolean) : Int {
    val nParts = Runtime.getRuntime().availableProcessors()
    val parts = partsList(values.size, nParts)

    val partialResults = IntArray(nParts) {0}

    val threads = parts.mapIndexed {
        resIndex, (firstIndex,lastIndex) ->
        var count = 0
        val t = Thread {
            for (idx in firstIndex until lastIndex) {
                if (pred(values[idx])) {
                    count++
                }
            }
            partialResults[resIndex] = count
        }
        t.start()
        t
    }
    threads.forEach {
        thread -> thread.join()
    }
    return partialResults.sum()
}

/**
 * An auxiliary function to generate the simulated texts to search
 */
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
    val values = Array<String>(4_000_000) { s }

    println("Start sequential search")
    test(::search, values, "serial" ) {
        it.equals(sRef)
    }
    println("Start parallel search")
    test(::psearch, values,"parallel" ) {
        it.equals(sRef)
    }
}