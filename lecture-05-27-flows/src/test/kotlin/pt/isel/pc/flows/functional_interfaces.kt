package pt.isel.pc.flows

import kotlin.test.Test

class FunctionalInterfacesTests {
    fun interface A {
        fun f1(a: Int)
        
        fun f2(a: Int): Int {
            println(this)
            f1(3)
            return a + 1
        }
    }
    
    fun A.f3(i: Int): Int {
        println("In a , this=$this")
        f1(i)
        return i + 2
    }
    
    @Test
    fun `test implement a function interface with a lambda`() {
        //        val a = A  { v ->
        //            println(v)
        //        }
        
        val a = object : A {
            override fun f1(a: Int) {
                println(a)
            }
            
        }
        println("a = ${a.javaClass}")
        a.f3(3)
    }
}
