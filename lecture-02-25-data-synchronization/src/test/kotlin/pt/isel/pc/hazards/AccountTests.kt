package pt.isel.pc.hazards

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

class AccountTests {
    private val INITIAL_BALANCE = 20000
    private val TRANSFER_VALUE = 1000
    private val NACCOUNTS = 16

    private fun multipleTransfersBetweenAccounts(accounts: Array<Account>,
                                                 transferOp : (src:Account, dst:Account) -> Unit) {
        require(accounts.size % 2 == 0)

        val nPairs = accounts.size / 2
        val nTransfers = 1000
        val threads = mutableListOf<Thread>()

        repeat(nPairs) {
            nPair ->
            val t1 = Thread {
                repeat(nTransfers) {

                    transferOp(accounts[2 * nPair], accounts[2 * nPair + 1])
                }
            }
            val t2 = Thread {
                repeat(nTransfers) {

                    transferOp(accounts[2 * nPair+ 1], accounts[2 * nPair ])
                }
            }
            threads.add(t1)
            threads.add(t2)
        }

        threads.forEach { it.start() }
        threads.forEach() { it.join() }

    }

    private fun doTest(name:String, transferOp : (src:Account, dst:Account) -> Unit) {
        val accounts = Array(NACCOUNTS) { Account(INITIAL_BALANCE) }
        val millis = measureTimeMillis {
            multipleTransfersBetweenAccounts(accounts, transferOp)
        }
        println("${name} test elapsed time: $millis m")
        accounts.forEach {
            assertEquals(INITIAL_BALANCE, it.getBalance())
        }
    }

    @Test
    fun `multiple thread account access without any synchronization`() {
        doTest("transferTo0") {
              src, dst -> src.transferTo0(dst, TRANSFER_VALUE)
        }
    }

    @Test
    fun `multiple thread account access using shared lock synchronization`() {
        doTest("transferTo1") {
                src, dst -> src.transferTo1(dst, TRANSFER_VALUE)
        }

    }

    @Test
    fun `multiple thread account access using account locks synchronization`() {
        doTest("transferTo2") {
                src, dst -> src.transferTo2(dst, TRANSFER_VALUE)
        }
    }

    @Test
    fun `multiple thread account access using account locks ordered by hashcode`() {
        doTest("transferTo3") {
                src, dst -> src.transferTo3(dst, TRANSFER_VALUE)
        }
    }

}