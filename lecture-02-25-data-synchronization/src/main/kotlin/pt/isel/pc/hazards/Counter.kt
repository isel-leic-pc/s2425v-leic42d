package pt.isel.pc.hazards

class Counter(private var value: Long = 0) {

    fun inc() {
        value++
    }
    fun dec() {
        if (value > 0) {
            value--
        }
    }

    fun get() : Long {
        return value
    }
}


