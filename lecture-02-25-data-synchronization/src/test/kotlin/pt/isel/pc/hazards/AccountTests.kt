package pt.isel.pc.hazards

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

class AccountTests {
    private val INITIAL_BALANCE = 200000
    private val TRANSFER_VALUE = 100
    private val NACCOUNTS = 50

    /**
     *
     */
    private fun multipleTransfersBetweenAccounts(accounts: Array<Account>,
                                                 transferOp : (src:Account, dst:Account) -> Unit) {
        require(accounts.size % 2 == 0)

        val nPairs = accounts.size / 2
        val nTransfers = 200
        val threads = mutableListOf<Thread>()

        repeat(nPairs) {
            nPair ->
            // each thread of accounts will transfer money in both directions
            // this way we avoid reach negative balances
            val t1 = Thread {
                repeat(nTransfers) {
                    transferOp(accounts[2 * nPair], accounts[2 * nPair + 1])
                    transferOp(accounts[2 * nPair+1], accounts[2 * nPair])
                }
            }
            val t2 = Thread {
                repeat(nTransfers) {
                    transferOp(accounts[2 * nPair+ 1], accounts[2 * nPair ])
                    transferOp(accounts[2 * nPair ], accounts[2 * nPair + 1])
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
        println("${name} test elapsed time: $millis ms!")
        accounts.forEach {
            account ->
            assertEquals(INITIAL_BALANCE, account.getBalance())
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