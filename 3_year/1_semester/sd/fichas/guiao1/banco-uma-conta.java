package guiao1.repeat;

import java.util.concurrent.locks.ReentrantLock;

class Bank {

    private static class Account {
        private int balance;
        private ReentrantLock lock;

        Account(int balance) {
            this.balance = balance;
            this.lock = new ReentrantLock();
        }

        int balance() {
            try {
                lock.lock();
                return balance;
            } finally {
                lock.unlock();
            }
        }

        boolean deposit(int value) {
            try {
                lock.lock();
                balance += value;
                return true;
            } finally {
                lock.unlock();
            }
        }
    }

    // Our single account, for now
    private Account savings = new Account(0);

    // Account balance
    public int balance() {
        return savings.balance();
    }

    // Deposit
    boolean deposit(int value) {
        return savings.deposit(value);
    }

}
