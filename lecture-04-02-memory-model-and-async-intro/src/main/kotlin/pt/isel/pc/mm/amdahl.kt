package pt.isel.pc.mm


fun main() {
    val s  = 0.0
    val n  = 200000
    val speedUp: Double = 1/(s + (1-s)/n)

    println("speedup = $speedUp")
}