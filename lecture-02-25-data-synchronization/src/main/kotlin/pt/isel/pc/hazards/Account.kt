package pt.isel.pc.hazards

import java.lang.Thread.sleep
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class Account( initialBalance: Int) {

    companion object {
        val globalMutex = ReentrantLock()
    }
    private var balance = initialBalance
    private val lock = ReentrantLock()

    fun deposit(amount: Int) {
        balance += amount
    }

    fun withdraw(amount: Int) {
        balance -= amount
    }

    fun getBalance(): Int {
        return balance
    }

    fun transferTo0(account: Account, amount: Int) {
        account.deposit(amount)
        sleep(1)
        withdraw(amount)
    }


    fun transferTo1(account: Account, amount: Int) {
        globalMutex.withLock {

            account.deposit(amount)
            sleep(1)
            withdraw(amount)
        }
    }

    fun transferTo2(account: Account, amount: Int) {
        lock.withLock {
            account.lock.withLock {
                account.deposit(amount)
                sleep(1)
                withdraw(amount)
            }
        }
    }

    fun transferTo3(account: Account, amount: Int) {
        val lock1 = if (lock.hashCode() > account.lock.hashCode()) { lock } else { account.lock }
        val lock2 = if (lock.hashCode() < account.lock.hashCode()) { lock } else { account.lock }
        lock1.withLock {
            lock2.withLock {

                account.deposit(amount)
                sleep(1)
                withdraw(amount)
            }
        }
    }

}