package pt.isel.pc.basicthreads

import java.io.BufferedWriter
import kotlin.math.min
import kotlin.system.measureTimeMillis

/**
 * Divides a range of items into a list of subranges, where each sub-range
 * is represented as a pair of indices.
 * Each sub-range attempts to distribute items evenly among the specified number of parts.
 *
 * @param count the total number of items to be divided into parts; must be greater than 0
 * @param nParts the number of parts to divide the items into; must be greater than 0
 * @return a list of pairs of integers, where each pair represents the start (inclusive) and end (exclusive)
 * indices of a part
 * @throws IllegalArgumentException if the preconditions for `count` and `nParts` are not met
 */
fun partsList(count: Int, nParts: Int) : List<Pair<Int,Int>> {
    require(count > 0 && nParts > 0 && count > 2*nParts)
    val step = (count - 1) / nParts + 1

    return generateSequence(Pair(0, step)) {
        seed -> Pair(seed.second, min(seed.second + step, count))
    }
    .take(nParts)
    .toList()
}

/**
 * Executes a given searching function multiple times and measures its performance
 * in terms of execution time, then prints the result with the minimum time taken
 * and the total matching elements found.
 *
 * @param T the type of elements in the input array
 * @param function a function that performs a search in the array and returns the count
 *        of elements satisfying a predicate. The provided function should accept an array
 *        of type T and a predicate function as parameters.
 * @param values the array of elements to be processed
 * @param prefix a string used as a prefix in the output message
 * @param pred a predicate function used to determine whether an element satisfies a condition
 */
fun <T> test( function : (Array<T>, (T) -> Boolean) -> Int,
          values: Array<T>, prefix: String, pred: (T) -> Boolean ) {
    var minTime = Long.MAX_VALUE
    var total = 0
    repeat(5) {
        var curTotal : Int
        val time = measureTimeMillis{
            curTotal =  function(values, pred)
        }
        if (time < minTime) {
            minTime = time
            total = curTotal
        }
    }

    println("$prefix found $total in ${minTime} ms!")
}

/**
 * Writes a line of text to the BufferedWriter, followed by flushing the stream to ensure the text is written immediately.
 *
 * @param line the line of text to be written
 */
fun BufferedWriter.writeLine(line: String) {
    appendLine(line)
    flush()
}