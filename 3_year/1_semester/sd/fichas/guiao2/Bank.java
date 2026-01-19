package guiao2.repeat;

import java.util.concurrent.locks.ReentrantLock;

public class Bank {

    public int totalBalance() {
        int total = 0;
        int acquired = 0;
        try {
            for (acquired = 0; acquired < slots; acquired++)
                av[acquired].lock.lock();

            for (int j = 0; j < slots; j++) {
                total += av[j].balance();
            }
        } finally {
            for (int k = 0; k < acquired; k++)
                av[k].lock.unlock();
        }

        return total;
    }

    public boolean transfer(int from, int to, int i) {
        if (from < 0 || to < 0 || from >= slots || to >= slots)
            return false;

        int min = Math.min(from, to);
        int max = Math.max(from, to);

        try {
            av[min].lock.lock();
            av[max].lock.lock();

            if (av[from].withdraw(i)) {
                av[to].deposit(i);
                return true;
            }
        } finally {
            av[min].lock.unlock();
            av[max].lock.unlock();
        }

        return false;
    }

    private static class Account {
        private ReentrantLock lock;
        private int balance;

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

        boolean withdraw(int value) {
            try {
                lock.lock();
                if (value > balance)
                    return false;
                balance -= value;
                return true;
            } finally {
                lock.unlock();
            }
        }
    }

    // Bank slots and vector of accounts
    private int slots;
    private Account[] av;

    public Bank(int n) {
        slots=n;
        av=new Account[slots];
        for (int i=0; i<slots; i++) av[i]=new Account(0);
    }

    // Account balance
    public int balance(int id) {
        if (id < 0 || id >= slots)
            return 0;
        return av[id].balance();
    }

    // Deposit
    public boolean deposit(int id, int value) {
        if (id < 0 || id >= slots)
            return false;
        return av[id].deposit(value);
    }

    // Withdraw; fails if no such account or insufficient balance
    public boolean withdraw(int id, int value) {
        if (id < 0 || id >= slots)
            return false;
        return av[id].withdraw(value);
    }
}
