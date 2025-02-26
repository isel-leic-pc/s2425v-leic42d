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

    /**
     * Deposits the specified amount into the account.
     * Also, that account is protected by a lock.
     * @param amount the amount to deposit
     */
    private fun deposit(amount: Int) {
        balance += amount
    }

    /**
     * Withdraws the specified amount from the account.
     * Assume that account has enough balance to withdraw the amount.
     * Also, that account is protected by a lock.
     * @param amount the amount to withdraw
     */
    private fun withdraw(amount: Int) : Boolean{
        if (balance < amount) {
            return false
        }
        else {
            balance -= amount
            return true
        }
    }

    fun getBalance(): Int {
        return balance
    }

    /**
     * A transfer version without synchronization.
     */
    fun transferTo0(account: Account, amount: Int) {
        if ( withdraw(amount)) {
            sleep(1)
            account.deposit(amount)
        }
    }


    /**
     * A transfer version with synchronization using a global mutex.
     */
    fun transferTo1(account: Account, amount: Int) {
        globalMutex.withLock {
            if (withdraw(amount)) {
                account.deposit(amount)
                sleep(1)
            }
        }
    }

    /**
     * A transfer version with synchronization using account locks.
     * this version is vulnerable to deadlock.
     */
    fun transferTo2(account: Account, amount: Int) {
        lock.withLock {
            account.lock.withLock {
                if (withdraw(amount)) {
                    sleep(1)
                    account.deposit(amount)
                }
            }
        }
    }

    /**
     * A transfer version with synchronization using account locks.
     * This version is deadlock free, since locks are acquired in a global order.
     */
    fun transferTo3(account: Account, amount: Int) {
        val lock1 = if (lock.hashCode() < account.lock.hashCode()) { lock } else { account.lock }
        val lock2 = if (lock.hashCode() > account.lock.hashCode()) { lock } else { account.lock }
        lock1.withLock {
            lock2.withLock {
                if (withdraw(amount)) {
                    sleep(1)
                    account.deposit(amount)
                }
            }
        }
    }
}