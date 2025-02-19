package pt.isel.pc.basicthreads

import kotlin.math.min

fun partsList(count: Int, nParts: Int) : List<Pair<Int,Int>> {
    require(count > 0 && nParts > 0 && count > 2*nParts)
    val step = (count - 1) / nParts + 1

    return generateSequence(Pair(0, step)) {
            seed -> Pair(seed.second, min(seed.second + step, count))
    }
    .take(nParts)
    .toList()
}